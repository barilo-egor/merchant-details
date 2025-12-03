package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MerchantDetailsService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry,
                                  MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer,
                                  MerchantConfigService merchantConfigService, VariableService variableService) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.merchantDetailsReceiveEventProducer = merchantDetailsReceiveEventProducer;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
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

    public Optional<DetailsResponse> getDetails(DetailsRequest request) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        List<MerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(request.getMethods(), request.getAmount());
        int attemptsCount = variableService.findByType(VariableType.ATTEMPTS_COUNT).getInt();
        for (int attemptNumber = 1; attemptNumber <= attemptsCount; attemptNumber++) {
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, attemptNumber);
            if (maybeDetailsResponse.isPresent()) break;
        }
        if (maybeDetailsResponse.isEmpty()) {
            log.debug("Реквизиты для сделки {} у мерчантов получены не были.", request.getId());
        }
        return maybeDetailsResponse;
    }

    private Optional<DetailsResponse> tryGetDetails(List<MerchantConfig> merchantConfigList, DetailsRequest request,
                                                    int attemptNumber) {
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        int index = 0;
        while (maybeDetailsResponse.isEmpty() && index < merchantConfigList.size()) {
            MerchantConfig merchantConfig = merchantConfigList.get(index);
            Merchant merchant = merchantConfig.getMerchant();
            try {
                log.debug("Попытка №{} мерчанта {} для сделки {}.", attemptNumber, merchant.name(), request.getId());
                maybeDetailsResponse = getDetails(merchant, request);
            } catch (Exception e) {
                log.debug("Ошибка получения реквизитов мерчанта {} для сделки №{} на попытке №{}: {}",
                        merchant.name(), request.getId(), attemptNumber, e.getMessage(), e);
            }
            index++;
        }
        maybeDetailsResponse.ifPresent(detailsResponse ->
                log.debug("Реквизиты для пользователя {} получены. Мерчант={}, реквизиты={}.",
                        request.getChatId(), detailsResponse.getMerchant().name(), detailsResponse)
        );
        return maybeDetailsResponse;
    }
}
