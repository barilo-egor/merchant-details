package net.rcetech.md.app.detailsapi.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientResponseException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.constants.Metrics;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantServiceRegistry;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.app.detailsapi.dto.ApiDetailsRequest;
import net.rcetech.md.meta.detailsapi.dto.ApiDetailsResponse;
import net.rcetech.md.meta.detailsapi.dto.Details;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import net.rcetech.md.domain.entity.ApiMerchantConfig;
import net.rcetech.md.meta.enums.ConfigType;
import net.rcetech.md.meta.enums.RequiredReceipt;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;
import net.rcetech.md.app.service.ApiMerchantConfigService;
import net.rcetech.md.integrations.service.RequestMethodService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.rcetech.md.app.service.MerchantDetailsService.MERCHANT;

@Service
@Slf4j
public class ApiMerchantDetailsService {

    public static final String STATUS = "status";

    private final MeterRegistry meterRegistry;

    private final ApiMerchantConfigService merchantConfigService;

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final RequestMethodService requestMethodService;

    public ApiMerchantDetailsService(MeterRegistry meterRegistry, ApiMerchantConfigService merchantConfigService,
                                     MerchantServiceRegistry merchantServiceRegistry, RequestMethodService requestMethodService) {
        this.meterRegistry = meterRegistry;
        this.merchantConfigService = merchantConfigService;
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.requestMethodService = requestMethodService;
    }

    @Timed(value = Metrics.GET_DETAILS_API, description = "Метрики api запросов на получение реквизитов.")
    public Optional<ApiDetailsResponse> getDetails(ApiDetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<ApiDetailsResponse> maybeDetailsResponse;
        List<ApiMerchantConfig> merchantConfigList = merchantConfigService.findAllTurnedByMerchantsAndOwnerId(
                requestMethodService.getMerchants(request.getRequestMethods()), request.getOwnerId());
        log.debug("Найденные мерчанты для api-запроса {}: {}", request.getInternalId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );

        maybeDetailsResponse = tryGetDetails(merchantConfigList, request);

        boolean hasDetails = maybeDetailsResponse.isPresent();
        String today = LocalDate.now().toString();
        if (!hasDetails) {
            meterRegistry.counter(
                    Metrics.GET_DETAILS_RESULT_API,
                    STATUS, "empty",
                    "date", today,
                    "configType", ConfigType.API.name()
            ).increment();
            log.debug("Реквизиты для api-сделки {} у мерчантов получены не были.", request.getInternalId());
        } else {
            meterRegistry.counter(Metrics.GET_DETAILS_RESULT_API, STATUS, "success",
                    "date", today,
                    "configType", ConfigType.API.name()).increment();
        }
        return maybeDetailsResponse;
    }

    private Optional<ApiDetailsResponse> tryGetDetails(List<ApiMerchantConfig> merchantConfigList, ApiDetailsRequest request) {
        if (CollectionUtils.isEmpty(merchantConfigList)) {
            return Optional.empty();
        }
        Optional<ApiDetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;

        final Instant deadline = Instant.now().plusSeconds(Math.min(request.getWaitTimeout(), 300));
        while (maybeDetailsResponse.isEmpty() && Instant.now().isBefore(deadline)) {
            Merchant merchant = merchantConfigList.get(index).getMerchant();
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                log.debug("Попытка мерчанта {} для api-сделки {}.", merchant.name(), request.getInternalId());
                maybeDetailsResponse = getDetails(merchant, request);
                sample.stop(meterRegistry.timer(Metrics.MERCHANT_GET_DETAILS_API, MERCHANT, merchant.name()));
                if (maybeDetailsResponse.isPresent()) {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "success").increment();
                } else {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "empty").increment();
                }
            } catch (Exception e) {
                log.debug("Ошибка получения реквизитов мерчанта {} для api-сделки №{}: {}",
                        merchant.name(), request.getInternalId(), e.getMessage(), e);
                meterRegistry.counter(Metrics.MERCHANT_RESULT_API, MERCHANT, merchant.name(), STATUS, "error").increment();
                if (e instanceof RestClientResponseException responseException) {
                    log.debug("Тело ответа ошибки для api-сделки №{}: {}", request.getInternalId(), responseException.getResponseBodyAsString());
                }
            }

            // Переход к следующему элементу с зацикливанием в начало списка
            index = (index + 1) % merchantConfigList.size();
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

        for (RequestMethod requestMethod : request.getRequestMethods()) {
            List<String> merchantMethods = requestMethodService.getMerchantMethods(merchant, List.of(requestMethod));
            if (CollectionUtils.isEmpty(merchantMethods)) {
                throw new MerchantMethodNotFoundException("Methods for merchant " + merchant.name() + " not found.");
            }

            for (String merchantMethod : merchantMethods) {
                OrderCreationRequest orderRequest = OrderCreationRequest.builder()
                        .requestId(request.getInternalId())
                        .id(request.getInternalId())
                        .amount(request.getAmount())
                        .userId(request.getUserId())
                        .method(merchantMethod)
                        .requiredReceipt(RequiredReceipt.NOT_REQUIRED)
                        .build();
                Optional<DetailsResponse> maybeDetailsResponse = maybeCreationService.get().createOrder(orderRequest);
                if (maybeDetailsResponse.isPresent()) {
                    DetailsResponse orderResponse = maybeDetailsResponse.get();
                    ApiDetailsResponse apiDetailsResponse = new ApiDetailsResponse();
                    apiDetailsResponse.setOrderId(orderResponse.getMerchantOrderId());
                    apiDetailsResponse.setOrderStatus(orderResponse.getMerchantOrderStatus());
                    apiDetailsResponse.setMerchant(orderResponse.getMerchant().name());
                    Details details = Details.builder()
                            .requestMethod(requestMethod)
                            .details(orderResponse.getDetails())
                            .bank(orderResponse.getBank())
                            .build();
                    apiDetailsResponse.setDetails(details);
                    apiDetailsResponse.setAmount(orderResponse.getAmount());
                    return Optional.of(apiDetailsResponse);
                }
            }
        }

        return Optional.empty();
    }

}
