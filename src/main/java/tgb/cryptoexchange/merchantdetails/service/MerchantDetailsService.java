package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.*;
import tgb.cryptoexchange.merchantdetails.dto.DetailsReceiveMonitorDTO;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantDetailsService {

    @Value("${kafka.topic.merchant-details.monitor}")
    private String detailsReceiveMonitorTopic;

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    private final SleepService sleepService;

    private final KafkaTemplate<String, DetailsReceiveMonitorDTO> detailsReceiveMonitorKafkaTemplate;

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry,
                                  @Autowired(required = false) MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer,
                                  MerchantConfigService merchantConfigService, VariableService variableService,
                                  SleepService sleepService,
                                  KafkaTemplate<String, DetailsReceiveMonitorDTO> detailsReceiveMonitorKafkaTemplate) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.merchantDetailsReceiveEventProducer = merchantDetailsReceiveEventProducer;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.sleepService = sleepService;
        this.detailsReceiveMonitorKafkaTemplate = detailsReceiveMonitorKafkaTemplate;
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
        return getDetails(request, Arrays.asList(Merchant.values()));
    }

    public Optional<DetailsResponse> getDetails(DetailsRequest request, Collection<Merchant> merchants) {
        log.debug("Получение реквизитов: {}", request.toString());
        Optional<DetailsResponse> maybeDetailsResponse = Optional.empty();
        List<MerchantConfig> merchantConfigList = merchantConfigService.findAllByMethodsAndAmount(
                merchants, request.getMethods(), request.getAmount()
        );
        log.debug("Найденные мерчанты для запроса по сделке {}: {}", request.getId(),
                merchantConfigList.stream()
                        .map(merchantConfig -> merchantConfig.getMerchant().name())
                        .collect(Collectors.joining(","))
        );
        int attemptsCount = variableService.findByType(VariableType.ATTEMPTS_COUNT).getInt();
        DetailsReceiveMonitor detailsReceiveMonitor = new DetailsReceiveMonitor(request.getId(), request.getAmount());
        for (int attemptNumber = 1; attemptNumber <= attemptsCount; attemptNumber++) {
            long t1 = System.currentTimeMillis();
            maybeDetailsResponse = tryGetDetails(merchantConfigList, request, attemptNumber, detailsReceiveMonitor);
            long t2 = System.currentTimeMillis();
            if (attemptNumber < attemptsCount) {
                long leftTime = (variableService.findByType(VariableType.MIN_ATTEMPT_TIME).getInt() * 1000) - (t2 - t1);
                if (leftTime > 0) {
                    sleepService.sleep(leftTime);
                }
            }
            if (maybeDetailsResponse.isPresent()) break;
        }
        boolean hasDetails = maybeDetailsResponse.isPresent();
        detailsReceiveMonitor.stop(hasDetails);
        try {
            detailsReceiveMonitorKafkaTemplate.send(detailsReceiveMonitorTopic, request.getRequestId(), detailsReceiveMonitor.toDTO());
        } catch (Exception e) {
            log.error("Ошибки при попытке отправить монитор в топик: {}", e.getMessage(), e);
        }
        if (hasDetails) {
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
