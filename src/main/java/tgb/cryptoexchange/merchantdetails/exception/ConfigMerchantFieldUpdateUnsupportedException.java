package tgb.cryptoexchange.merchantdetails.exception;

import tgb.cryptoexchange.exception.BadRequestException;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;

/**
 * Пробрасывается в случае, если в PATCH запрос для обновления было передано поле {@link MerchantConfigDTO#getMerchant()}
 */
public class ConfigMerchantFieldUpdateUnsupportedException extends BadRequestException {
    public ConfigMerchantFieldUpdateUnsupportedException(String message) {
        super(message);
    }
}
