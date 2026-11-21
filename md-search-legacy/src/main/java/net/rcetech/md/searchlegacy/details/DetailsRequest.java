package net.rcetech.md.searchlegacy.details;

/**
 * Общий контракт для запросов на получение реквизитов из различных источников системы.
 * <p>
 * Служит полиморфным представлением запроса для компонентов, которым не важна специфика
 * конкретного источника (например, для {@link net.rcetech.md.searchlegacy.kafka.MerchantDetailsReceiveEventProducer},
 * отправляющего событие об успешной выдаче реквизитов).
 * </p>
 * <p>
 * Реализация: {@link BotDetailsRequest} — запрос реквизитов от телеграм-ботов (через Kafka).
 */
public interface DetailsRequest {

    String getUserId();

    String getId();

    String getInitiatorApp();

    Integer getAmount();

}
