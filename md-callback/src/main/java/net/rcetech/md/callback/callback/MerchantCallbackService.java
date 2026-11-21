package net.rcetech.md.callback.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CallbackStatus;
import net.rcetech.md.integrations.platform.MerchantService;
import net.rcetech.md.integrations.platform.MerchantServiceRegistry;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.callback.kafka.MerchantCallbackEvent;

import java.util.Objects;
import java.util.Optional;

/**
 * Обработка callback'ов площадок: разбор статуса ордера площадкой, отправка статуса в Kafka
 * и удаление чека ордера.
 */
@Service
@Slf4j
public class MerchantCallbackService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    private final Environment environment;

    private KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate;

    public MerchantCallbackService(MerchantServiceRegistry merchantServiceRegistry, Environment environment) {
        this.merchantServiceRegistry = merchantServiceRegistry;
        this.environment = environment;
    }

    @Autowired(required = false)
    public void setCallbackKafkaTemplate(@Qualifier("callbackKafkaTemplate") KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate) {
        this.callbackKafkaTemplate = callbackKafkaTemplate;
    }

    /**
     * Обновляет статус ордера по телу callback'а площадки.
     *
     * @param merchant     площадка, приславшая callback
     * @param callbackBody тело callback'а, уже расшифрованное
     */
    public void updateStatus(Merchant merchant, String callbackBody) {
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isEmpty()) {
            log.warn("Запрос обновления статуса ордера мерчанта {}, у которого отсутствует реализация: {}",
                    merchant.name(), callbackBody);
            return;
        }
        MerchantService merchantService = maybeMerchantService.get();
        Optional<CallbackStatus> maybeCallbackStatus = merchantService.parseCallback(callbackBody);
        if (maybeCallbackStatus.isEmpty()) {
            return;
        }
        CallbackStatus callbackStatus = maybeCallbackStatus.get();
        send(merchant, callbackStatus, callbackBody);
        merchantService.deleteReceipt(callbackStatus.merchantOrderId(), callbackStatus.status());
    }

    /**
     * Запрашивает у площадки данные callback'а по транзакции и обновляет по ним статус ордера.
     *
     * @param merchant      площадка
     * @param transactionId идентификатор транзакции на стороне площадки
     */
    public void fetchStatus(Merchant merchant, String transactionId) {
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isEmpty()) {
            log.warn("Запрос получения статуса ордера мерчанта {}, у которого отсутствует реализация: {}",
                    merchant.name(), transactionId);
            return;
        }
        maybeMerchantService.get().fetchCallbackDataByTransactionId(transactionId)
                .ifPresent(callbackData -> updateStatus(merchant, callbackData));
    }

    private void send(Merchant merchant, CallbackStatus callbackStatus, String callbackBody) {
        if (Objects.isNull(callbackKafkaTemplate)) {
            return;
        }
        MerchantCallbackEvent event = new MerchantCallbackEvent();
        event.setMerchantOrderId(callbackStatus.merchantOrderId());
        event.setStatus(callbackStatus.status());
        event.setStatusDescription(callbackStatus.statusDescription());
        event.setMerchant(merchant);
        try {
            callbackKafkaTemplate.send(
                    environment.getRequiredProperty("kafka.topic.merchant-details.callback"),
                    merchant + "_" + callbackStatus.merchantOrderId(), event
            );
        } catch (Exception e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при попытке обновления статуса мерчанта {}. Статус={}, оригинальное тело={}. Message={}.",
                    currentTime, merchant.name(), callbackStatus, callbackBody, e.getMessage(), e);
            throw new ServiceUnavailableException("callback cannot be processed: " + currentTime);
        }
    }
}
