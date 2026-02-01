package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.*;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

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

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry,
                                  @Autowired(required = false) MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer,
                                  MerchantConfigService merchantConfigService, VariableService variableService,
                                  SleepService sleepService) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.merchantDetailsReceiveEventProducer = merchantDetailsReceiveEventProducer;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.sleepService = sleepService;
    }

    public Optional<DetailsResponse> getDetails(Merchant merchant, DetailsRequest request) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            Optional<DetailsResponse> maybeDetailsResponse = maybeCreationService.get().createOrder(request);
            if (Objects.nonNull(merchantDetailsReceiveEventProducer)) {
                maybeDetailsResponse.ifPresent(
                        detailsResponse -> merchantDetailsReceiveEventProducer.put(merchant, request, detailsResponse)
                );
            }
            return maybeDetailsResponse;
        }
        log.warn("Запрос получения реквизитов мерчанта {}, у которого отсутствует реализация: {}", merchant.name(), request.toString());
        return Optional.empty();
    }

    public void updateStatus(Merchant merchant, String callbackBody) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            maybeCreationService.get().updateStatus(callbackBody);
        } else {
            log.warn("Запрос обновления статуса ордера мерчанта {}, у которого отсутствует реализация: {}",
                    merchant.name(), callbackBody);
        }
    }

    public void cancelOrder(Merchant merchant, CancelOrderRequest cancelOrderRequest) {
        log.debug("Запрос на отмену ордера мерчанта {}: {}", merchant.name(), cancelOrderRequest.toString());
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            maybeCreationService.get().cancelOrder(cancelOrderRequest);
        } else {
            log.warn("Запрос отмены ордера мерчанта {}, у которого отсутствует реализация: {}",
                    merchant.name(), cancelOrderRequest);
        }
    }

    public Optional<DetailsResponse> getDetails(DetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        List<MerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(request.getMethods(), request.getAmount());
        log.debug("Найденные мерчанты для запроса по сделке {}: {}", request.getId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );
        int attemptsCount = variableService.findByType(VariableType.ATTEMPTS_COUNT).getInt();
        DetailsReceiveMonitor detailsReceiveMonitor = new DetailsReceiveMonitor(request.getId(), request.getAmount());
        for (int attemptNumber = 1; attemptNumber <= attemptsCount; attemptNumber++) {
            if (Thread.currentThread().isInterrupted()) {
                log.debug("Поиск реквизитов для сделки {} был прерван.", request.getId());
                return Optional.empty();
            }
            long t1 = System.currentTimeMillis();
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, attemptNumber, detailsReceiveMonitor);
            long t2 = System.currentTimeMillis();
            if (attemptNumber < attemptsCount && maybeDetailsResponse.isEmpty()) {
                long leftTime = (variableService.findByType(VariableType.MIN_ATTEMPT_TIME).getInt() * 1000) - (t2 - t1);
                if (leftTime > 0) {
                    sleepService.sleep(leftTime);
                }
            }
            if (maybeDetailsResponse.isPresent()) break;
        }
        boolean hasDetails = maybeDetailsResponse.isPresent();
        detailsReceiveMonitor.stop(hasDetails);
        if (!hasDetails) {
            log.debug("Реквизиты для сделки {} у мерчантов получены не были.", request.getId());
        }
        return maybeDetailsResponse;
    }

    private Optional<DetailsResponse> tryGetDetails(List<MerchantConfig> merchantConfigList, DetailsRequest request,
                                                    int attemptNumber, DetailsReceiveMonitor detailsReceiveMonitor) {
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;
        while (maybeDetailsResponse.isEmpty() && index < merchantConfigList.size()) {
            Merchant merchant = merchantConfigList.get(index).getMerchant();
            String method = request.getMerchantMethod(merchant).orElseThrow(
                    () -> new MerchantMethodNotFoundException("Method for merchant " + merchant.name() + " not found.")
            );
            var merchantAttempt = detailsReceiveMonitor.start(merchant, method);
            try {
                log.debug("Попытка №{} мерчанта {} для сделки {}.", attemptNumber, merchant.name(), request.getId());
                maybeDetailsResponse = getDetails(merchant, request);
                merchantAttempt.stop(maybeDetailsResponse.isPresent());
            } catch (Exception e) {
                merchantAttempt.error();
                log.debug("Ошибка получения реквизитов мерчанта {} для сделки №{} на попытке №{}: {}",
                        merchant.name(), request.getId(), attemptNumber, e.getMessage(), e);
                if (e instanceof WebClientResponseException responseException) {
                    log.debug("Тело ответа ошибки для сделки №{}: {}", request.getId(), responseException.getResponseBodyAsString());
                }
            }
            index++;
        }
        maybeDetailsResponse.ifPresent(detailsResponse ->
                log.debug("Реквизиты для пользователя {} получены. Мерчант={}, реквизиты={}.",
                        request.getChatId(), detailsResponse.getMerchant().name(), detailsResponse)
        );
        return maybeDetailsResponse;
    }

    public void sendReceipt(Merchant merchant, String orderId, MultipartFile file) {
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isPresent()) {
            maybeMerchantService.get().sendReceipt(orderId, file);
        } else {
            log.debug("Отсутствует реализация для мерчанта {}. Чек по ордеру {} отправлен не будет", merchant.name(), orderId);
        }
    }
}
