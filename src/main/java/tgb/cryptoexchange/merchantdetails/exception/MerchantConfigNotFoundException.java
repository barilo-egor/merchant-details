package tgb.cryptoexchange.merchantdetails.exception;

/**
 * Пробрасывается в случае, если запрошенный {@link tgb.cryptoexchange.merchantdetails.entity.MerchantConfig} не был найден
 */
public class MerchantConfigNotFoundException extends RuntimeException {
    public MerchantConfigNotFoundException(String message) {
        super(message);
    }
}
