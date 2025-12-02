package tgb.cryptoexchange.merchantdetails.details;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantRequisiteService {

    private final MerchantConfigService merchantConfigService;

    private final MerchantDetailsService merchantDetailsService;

    public MerchantRequisiteService(MerchantConfigService merchantConfigService,
                                    MerchantDetailsService merchantDetailsService) {
        this.merchantConfigService = merchantConfigService;
        this.merchantDetailsService = merchantDetailsService;
    }

    public Optional<DetailsResponse> getRequisites(MethodsDetailsRequest request) {
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
                    DetailsRequest detailsRequest = new DetailsRequest();
                    detailsRequest.setId(request.getId());
                    detailsRequest.setInitiatorApp(request.getInitiatorApp());
                    detailsRequest.setAmount(request.getAmount());
                    detailsRequest.setMethod(request.getMethod(merchant)
                            .orElseThrow(
                                    () -> new MerchantMethodNotFoundException("Method for merchant " + merchant.name() + " not found")
                            ));
                    detailsRequest.setChatId(request.getChatId());
                    maybeDetailsResponse = merchantDetailsService.getDetails(merchant, detailsRequest);
                    if (maybeDetailsResponse.isPresent()) {
                        log.debug("Реквизиты для пользователя {} получены. Мерчант={}, реквизиты={}.",
                                detailsRequest.getChatId(), merchant.name(), maybeDetailsResponse.get());
                        break;
                    } else {
                        log.debug("Реквизиты для сделки {} мерчанта {} с попытки №{} не получены.",
                                detailsRequest.getId(), merchant.name(), i + 1);
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
