package net.rcetech.md.meta.projection;

import net.rcetech.md.meta.constants.AutoConfirmType;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;

/**
 * Настройка автоподтверждения сделок мерчанта.
 */
public interface AutoConfirmConfigProjection {
    CryptoCurrency getCryptoCurrency();
    AutoConfirmType getAutoConfirmType();
    DeliveryType getDeliveryType();
}
