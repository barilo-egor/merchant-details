package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantDetailsService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    private final MerchantConfigService merchantConfigService;

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry,
                                  MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer,
                                  MerchantConfigService merchantConfigService) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.merchantDetailsReceiveEventProducer = merchantDetailsReceiveEventProducer;
        this.merchantConfigService = merchantConfigService;
    }

    public Optional<DetailsResponse> getDetails(Merchant merchant, DetailsRequest request) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            Optional<DetailsResponse> maybeDetailsResponse = maybeCreationService.get().createOrder(request);
            maybeDetailsResponse.ifPresent(
                    detailsResponse -> merchantDetailsReceiveEventProducer.put(merchant, request, detailsResponse)
            );
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

    public Optional<DetailsResponse> getRequisites(DetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        List<MerchantConfig> merchantConfigList = merchantConfigService.findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> request.getAmount() <= config.getMaxAmount())
                .filter(config -> {
                    Optional<String> method = request.getMethod(config.getMerchant());
                    return method.isPresent();
                })
                .toList();
        int maxAttemptCount = 5;
        for (MerchantConfig merchantConfig : merchantConfigList) {
            if (merchantConfig.getAttemptsCount() > maxAttemptCount) {
                maxAttemptCount = merchantConfig.getAttemptsCount();
            }
        }
        List<Merchant> merchantList = merchantConfigList.stream().map(MerchantConfig::getMerchant).toList();
        log.debug("Список мерчантов для сделки {}: {}", request.getId(),
                merchantList.stream().map(Merchant::name).collect(Collectors.joining(", ")));
        for (int i = 0; i < maxAttemptCount; i++) {
            for (MerchantConfig merchantConfig : merchantConfigList) {
                Merchant merchant = merchantConfig.getMerchant();
                try {
                    int merchantAttemptsCount = merchantConfig.getAttemptsCount();
                    if (merchantAttemptsCount < i + 1) {
                        continue;
                    }
                    log.debug("Попытка №{} мерчанта {} для сделки {}.", i + 1, merchant.name(), request.getId());
                    maybeDetailsResponse = getDetails(merchant, request);
                    if (maybeDetailsResponse.isPresent()) {
                        log.debug("Реквизиты для пользователя {} получены. Мерчант={}, реквизиты={}.",
                                request.getChatId(), merchant.name(), maybeDetailsResponse.get());
                        break;
                    } else {
                        log.debug("Реквизиты для сделки {} мерчанта {} с попытки №{} не получены.",
                                request, merchant.name(), i + 1);
                    }
                } catch (Exception e) {
                    log.debug("Ошибка получения реквизитов мерчанта {} для сделки №{} c попытки №{}: {}",
                            merchant.name(), request.getId(), i + 1, e.getMessage(), e);
                }
                try {
                    if (i < merchantConfig.getAttemptsCount() - 1) {
                        Thread.sleep(merchantConfig.getDelay() * 1000L);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            if (maybeDetailsResponse.isPresent()) break;
        }
        if (maybeDetailsResponse.isEmpty()) {
            log.debug("Реквизиты для сделки {} у мерчантов получены не были.", request.getId());
        }
        return maybeDetailsResponse;
    }
}
