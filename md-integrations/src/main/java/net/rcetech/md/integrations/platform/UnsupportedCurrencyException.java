package net.rcetech.md.integrations.platform;

import net.rcetech.md.meta.enums.FiatCurrency;
import net.rcetech.md.meta.merchant.Platform;
import tgb.cryptoexchange.commons.enums.Merchant;

/**
 * Мерчант работает в валюте, которую его площадка не поддерживает.
 * Ошибка создания ордера вместо тихой подмены валюты.
 */
public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(Merchant merchant, Platform platform, FiatCurrency currency) {
        super("Площадка " + platform + " не поддерживает валюту " + currency + " мерчанта " + merchant);
    }
}
