package tgb.cryptoexchange.merchantdetails.details;


/**
 * Общий контракт для запросов на получение реквизитов из различных источников системы.
 * <p>
 * Служит полиморфным представлением запроса для компонентов, которым не важна специфика
 * конкретного источника (например, для {@link tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer},
 * отправляющего событие об успешной выдаче реквизитов).
 * </p>
 * <p>
 * Основные реализации:
 * <ul>
 *     <li>{@link BotDetailsRequest} — запрос реквизитов от телеграм-ботов (через Kafka).</li>
 *     <li>{@link tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest} — запрос реквизитов через внешний API / gRPC.</li>
 * </ul>
 */
public interface DetailsRequest {

    String getUserId();

    String getId();

    String getInitiatorApp();

    Integer getAmount();

}
