package net.rcetech.md.app.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientResponseException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.constants.Metrics;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.app.details.*;
import net.rcetech.md.integrations.platform.*;
import net.rcetech.md.domain.entity.MerchantConfig;
import net.rcetech.md.meta.enums.ConfigType;
import net.rcetech.md.meta.enums.RequiredReceipt;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;
import net.rcetech.md.app.kafka.MerchantDetailsReceiveEventProducer;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantDetailsService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    private final SleepService sleepService;

    private final MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    private final MeterRegistry meterRegistry;

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry,
                                  @Autowired(required = false) MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer,
                                  MerchantConfigService merchantConfigService, VariableService variableService,
                                  SleepService sleepService, MeterRegistry meterRegistry) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.merchantDetailsReceiveEventProducer = merchantDetailsReceiveEventProducer;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.sleepService = sleepService;
        this.meterRegistry = meterRegistry;
    }

    public Optional<DetailsResponse> getDetails(Merchant merchant, RequiredReceipt requiredReceipt, BotDetailsRequest request) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isEmpty()) {
            log.warn("Запрос получения реквизитов мерчанта {}, у которого отсутствует реализация: {}", merchant.name(), request.toString());
            return Optional.empty();
        }
        List<String> merchantMethods = request.getMerchantMethod(merchant);
        if (CollectionUtils.isEmpty(merchantMethods)) {
            throw new MerchantMethodNotFoundException("Methods for merchant " + merchant.name() + " not found.");
        }

        for (String merchantMethod : merchantMethods) {
            OrderCreationRequest orderRequest = OrderCreationRequest.builder()
                    .requestId(request.getRequestId())
                    .id(request.getId())
                    .amount(request.getAmount())
                    .userId(request.getUserId())
                    .method(merchantMethod)
                    .redirectUrl(request.getRedirectUrl())
                    .phone(request.getPhone())
                    .requiredReceipt(requiredReceipt)
                    .build();
            Optional<DetailsResponse> maybeDetailsResponse = maybeCreationService.get().createOrder(orderRequest);
            if (maybeDetailsResponse.isPresent()) {
                if (Objects.nonNull(merchantDetailsReceiveEventProducer)) {
                    merchantDetailsReceiveEventProducer.put(merchant, merchantMethod, request, maybeDetailsResponse.get());
                }
                if (StringUtils.isBlank(maybeDetailsResponse.get().getPaymentMethod())) {
                    maybeDetailsResponse.get().setPaymentMethod(merchantMethod);
                }
                return maybeDetailsResponse;
            }
        }
        return Optional.empty();
    }

    @Timed(value = Metrics.GET_DETAILS, description = "Метрики запросов на получение реквизитов.")
    public Optional<DetailsResponse> getDetails(BotDetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        List<MerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(request.getMethods(), request.getAmount());
        log.debug("Найденные мерчанты для запроса по сделке {}: {}", request.getId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );
        int attemptsCount = variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT).getInt();
        for (int attemptNumber = 1; attemptNumber <= attemptsCount && !merchantConfigList.isEmpty(); attemptNumber++) {
            if (Thread.currentThread().isInterrupted()) {
                log.debug("Поиск реквизитов для сделки {} был прерван.", request.getId());
                return Optional.empty();
            }
            long t1 = System.currentTimeMillis();
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, attemptNumber);
            long t2 = System.currentTimeMillis();
            if (attemptNumber < attemptsCount && maybeDetailsResponse.isEmpty()) {
                long leftTime = (variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT).getInt() * 1000) - (t2 - t1);
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
                    Metrics.TAG_STATUS, "empty",
                    "initiatorApp", request.getInitiatorApp(),
                    "date", today
            ).increment();
            log.debug("Реквизиты для сделки {} у мерчантов получены не были.", request.getId());
        } else {
            meterRegistry.counter(Metrics.GET_DETAILS_RESULT, Metrics.TAG_STATUS, "success",
                    "initiatorApp", request.getInitiatorApp(),
                    "date", today).increment();
        }
        return maybeDetailsResponse;
    }

    private Optional<DetailsResponse> tryGetDetails(List<MerchantConfig> merchantConfigList, BotDetailsRequest request,
                                                    int attemptNumber) {
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;
        while (maybeDetailsResponse.isEmpty() && index < merchantConfigList.size()) {
            MerchantConfig merchantConfig = merchantConfigList.get(index);
            Merchant merchant = merchantConfig.getMerchant();
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                log.trace("Попытка №{} мерчанта {} для сделки {}.", attemptNumber, merchant.name(), request.getId());
                maybeDetailsResponse = getDetails(merchant, merchantConfig.getRequiredReceipt(), request);
                sample.stop(meterRegistry.timer(Metrics.MERCHANT_GET_DETAILS, Metrics.TAG_MERCHANT, merchant.name()));
                if (maybeDetailsResponse.isPresent()) {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT, Metrics.TAG_MERCHANT, merchant.name(), Metrics.TAG_STATUS, "success").increment();
                } else {
                    meterRegistry.counter(Metrics.MERCHANT_RESULT, Metrics.TAG_MERCHANT, merchant.name(), Metrics.TAG_STATUS, "empty").increment();
                }
            } catch (Exception e) {
                log.debug("Ошибка получения реквизитов мерчанта {} для сделки №{} на попытке №{}: {}",
                        merchant.name(), request.getId(), attemptNumber, e.getMessage());
                meterRegistry.counter(Metrics.MERCHANT_RESULT, Metrics.TAG_MERCHANT, merchant.name(), Metrics.TAG_STATUS, "error").increment();
                if (e instanceof RestClientResponseException responseException) {
                    log.debug("Тело ответа ошибки для сделки №{}: {}", request.getId(), responseException.getResponseBodyAsString());
                }
            }
            index++;
        }
        maybeDetailsResponse.ifPresent(detailsResponse ->
                log.debug("Реквизиты для пользователя {} получены. Мерчант={}, реквизиты={}.",
                        request.getUserId(), detailsResponse.getMerchant().name(), detailsResponse)
        );
        return maybeDetailsResponse;
    }
}
