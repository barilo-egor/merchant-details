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
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
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

    @Timed(value = Metrics.GET_DETAILS, description = "Метрики api запросов на получение реквизитов.")
    public Optional<ApiDetailsResponse> getDetails(ApiDetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<ApiDetailsResponse> maybeDetailsResponse = Optional.empty();
        List<ApiMerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(request.getRequestMethods(), request.getAmount());
        log.debug("Найденные мерчанты для api-запроса {}: {}", request.getRequestId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );
        int attemptsCount = variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.API).getInt();

        for (int attemptNumber = 1; attemptNumber <= attemptsCount && !merchantConfigList.isEmpty(); attemptNumber++) {
            long t1 = System.currentTimeMillis();
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, attemptNumber);
            long t2 = System.currentTimeMillis();
            if (attemptNumber < attemptsCount && maybeDetailsResponse.isEmpty()) {
                long leftTime = (variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.API).getInt() * 1000) - (t2 - t1);
                if (leftTime > 0) {
                    sleepService.sleep(leftTime);
                }
            }
            if (maybeDetailsResponse.isPresent()) break;
        }
        boolean hasDetails = maybeDetailsResponse.isPresent();
        String today = LocalDate.now().toString();
        if (!hasDetails) {
            meterRegistry.counter(
                    Metrics.GET_DETAILS_RESULT,
                    STATUS, "empty",
                    "date", today,
                    "configType", ConfigType.API.name()
            ).increment();
            log.debug("Реквизиты для api-сделки {} у мерчантов получены не были.", request.getRequestId());
        } else {
            meterRegistry.counter(Metrics.GET_DETAILS_RESULT, STATUS, "success",
                    "date", today,
                    "configType", ConfigType.API.name()).increment();
        }
        return maybeDetailsResponse;
    }

    private Optional<ApiDetailsResponse> tryGetDetails(List<ApiMerchantConfig> merchantConfigList, ApiDetailsRequest request, int attemptNumber) {
        Optional<ApiDetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;
        while (maybeDetailsResponse.isEmpty() && index < merchantConfigList.size()) {
            Merchant merchant = merchantConfigList.get(index).getMerchant();
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                log.debug("Попытка №{} мерчанта {} для api-сделки {}.", attemptNumber, merchant.name(), request.getRequestId());
                maybeDetailsResponse = getDetails(merchant, request);
                sample.stop(meterRegistry.timer(Metrics.MERCHANT_GET_DETAILS, MERCHANT, merchant.name()));
                if (maybeDetailsResponse.isPresent()) {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT, MERCHANT, merchant.name(), STATUS, "success").increment();
                } else {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT, MERCHANT, merchant.name(), STATUS, "empty").increment();
                }
            } catch (Exception e) {
                log.debug("Ошибка получения реквизитов мерчанта {} для api-сделки №{} на попытке №{}: {}",
                        merchant.name(), request.getRequestId(), attemptNumber, e.getMessage(), e);
                meterRegistry.counter(Metrics.MERCHANT_RESULT, MERCHANT, merchant.name(), STATUS, "error").increment();
                if (e instanceof WebClientResponseException responseException) {
                    log.debug("Тело ответа ошибки для api-сделки №{}: {}", request.getRequestId(), responseException.getResponseBodyAsString());
                }
            }
            index++;
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
                Details details = Details.builder()
                        .requestMethod(RequestMethod.valueOf(merchantMethod))
                        .details(orderResponse.getDetails())
                        .bank(orderResponse.getBank())
                        .operator(orderResponse.getOperator())
                        .build();
                apiDetailsResponse.setDetails(details);
                return Optional.of(apiDetailsResponse);
            }
        }
        return Optional.empty();
    }

}
