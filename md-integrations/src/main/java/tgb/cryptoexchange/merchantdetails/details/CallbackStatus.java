package tgb.cryptoexchange.merchantdetails.details;

/**
 * Статус ордера, разобранный из callback'а площадки.
 *
 * @param merchantOrderId   идентификатор ордера на стороне площадки
 * @param status            статус ордера в терминах площадки
 * @param statusDescription описание статуса
 */
public record CallbackStatus(String merchantOrderId, String status, String statusDescription) {
}
