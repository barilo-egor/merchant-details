package tgb.cryptoexchange.merchantdetails.detailsapi.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.Metrics;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.Details;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.service.ApiMerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.SleepService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService.MERCHANT;

@Service
@Slf4j
public class ApiMerchantDetailsService {

    public static final String STATUS = "status";

    private final MeterRegistry meterRegistry;

    private final ApiMerchantConfigService merchantConfigService;

    private final VariableService variableService;

    private final SleepService sleepService;

    private final MerchantServiceRegistry merchantServiceRegistry;

    public ApiMerchantDetailsService(MeterRegistry meterRegistry, ApiMerchantConfigService merchantConfigService,
                                     VariableService variableService, SleepService sleepService,
                                     MerchantServiceRegistry merchantServiceRegistry) {
        this.meterRegistry = meterRegistry;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.sleepService = sleepService;
        this.merchantServiceRegistry = merchantServiceRegistry;
    }

    @Timed(value = Metrics.GET_DETAILS_API, description = "Метрики api запросов на получение реквизитов.")
    public Optional<ApiDetailsResponse> getDetails(ApiDetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<ApiDetailsResponse> maybeDetailsResponse;
        List<ApiMerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(request.getRequestMethods(), request.getAmount());
        log.debug("Найденные мерчанты для api-запроса {}: {}", request.getRequestId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );

        final Instant timeoutTime = Instant.now().plusSeconds(request.getWaitTimeout());

        do {
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, timeoutTime);
            if (maybeDetailsResponse.isPresent()) break;
        } while (Instant.now().compareTo(timeoutTime) > 0);

        boolean hasDetails = maybeDetailsResponse.isPresent();
        String today = LocalDate.now().toString();
        if (!hasDetails) {
            meterRegistry.counter(
                    Metrics.GET_DETAILS_RESULT_API,
                    STATUS, "empty",
                    "date", today,
                    "configType", ConfigType.API.name()
            ).increment();
            log.debug("Реквизиты для api-сделки {} у мерчантов получены не были.", request.getRequestId());
        } else {
            meterRegistry.counter(Metrics.GET_DETAILS_RESULT_API, STATUS, "success",
                    "date", today,
                    "configType", ConfigType.API.name()).increment();
        }
        return maybeDetailsResponse;
    }

    private Optional<ApiDetailsResponse> tryGetDetails(List<ApiMerchantConfig> merchantConfigList, ApiDetailsRequest request,
                                                       Instant timeoutTime) {
        Optional<ApiDetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;
        while (maybeDetailsResponse.isEmpty() && index < merchantConfigList.size()) {
            Merchant merchant = merchantConfigList.get(index).getMerchant();
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                log.debug("Попытка мерчанта {} для api-сделки {}.", merchant.name(), request.getRequestId());
                maybeDetailsResponse = getDetails(merchant, request);
                sample.stop(meterRegistry.timer(Metrics.MERCHANT_GET_DETAILS_API, MERCHANT, merchant.name()));
                if (maybeDetailsResponse.isPresent()) {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "success").increment();
                } else {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "empty").increment();
                }
            } catch (Exception e) {
                log.debug("Ошибка получения реквизитов мерчанта {} для api-сделки №{}: {}",
                        merchant.name(), request.getRequestId(), e.getMessage(), e);
                meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "error").increment();
                if (e instanceof WebClientResponseException responseException) {
                    log.debug("Тело ответа ошибки для api-сделки №{}: {}", request.getRequestId(), responseException.getResponseBodyAsString());
                }
            }
            index++;
            if (Instant.now().compareTo(timeoutTime) > 0) {
                break;
            }
        }
        maybeDetailsResponse.ifPresent(detailsResponse ->
                log.debug("Реквизиты для пользователя {} получены. Реквизиты={}.", request.getUserId(), detailsResponse)
        );
        return maybeDetailsResponse;
    }

    public Optional<ApiDetailsResponse> getDetails(Merchant merchant, ApiDetailsRequest request) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isEmpty()) {
            log.warn("Запрос получения реквизитов мерчанта {}, у которого отсутствует реализация: {}", merchant.name(), request.toString());
            return Optional.empty();
        }

        List<String> merchantMethods = request.getMerchantMethods(merchant);
        if (CollectionUtils.isEmpty(merchantMethods)) {
            throw new MerchantMethodNotFoundException("Methods for merchant " + merchant.name() + " not found.");
        }

        for (String merchantMethod : merchantMethods) {
            OrderCreationRequest orderRequest = OrderCreationRequest.builder()
                    .requestId(request.getRequestId())
                    .id(request.getInternalId())
                    .amount(request.getAmount())
                    .userId(request.getUserId())
                    .method(merchantMethod).build();
            Optional<DetailsResponse> maybeDetailsResponse = maybeCreationService.get().createOrder(orderRequest);
            if (maybeDetailsResponse.isPresent()) {
                DetailsResponse orderResponse = maybeDetailsResponse.get();
                ApiDetailsResponse apiDetailsResponse = new ApiDetailsResponse();
                apiDetailsResponse.setRequestId(orderResponse.getRequestId());
                apiDetailsResponse.setOrderId(orderResponse.getMerchantOrderId());
                apiDetailsResponse.setOrderStatus(orderResponse.getMerchantOrderStatus());
                apiDetailsResponse.setMerchant(orderResponse.getMerchant().name());
                Details details = Details.builder()
                        .requestMethod(RequestMethod.valueOf(merchantMethod))
                        .details(orderResponse.getDetails())
                        .bank(orderResponse.getBank())
                        .operator(orderResponse.getOperator())
                        .build();
                apiDetailsResponse.setDetails(details);
                apiDetailsResponse.setAmount(orderResponse.getAmount());
                return Optional.of(apiDetailsResponse);
            }
        }
        return Optional.empty();
    }

}
