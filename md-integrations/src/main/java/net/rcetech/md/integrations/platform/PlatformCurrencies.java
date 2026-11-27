package net.rcetech.md.integrations.platform;

import net.rcetech.md.meta.enums.FiatCurrency;
import net.rcetech.md.meta.merchant.MerchantCatalog;
import tgb.cryptoexchange.commons.enums.Merchant;

/**
 * Соответствие «площадка → значение валюты в запросе» (код, регистр, числовой id).
 * Валюта берётся из справочника мерчантов; валюты, которой у площадки нет,
 * — ошибка создания ордера, а не тихая подмена на рубль.
 */
public final class PlatformCurrencies {

    private PlatformCurrencies() {
    }

    /**
     * Код валюты в проводном формате площадки: "RUB" (верхний регистр) или "rub" (нижний).
     */
    public static String code(Merchant merchant) {
        MerchantCatalog entry = MerchantCatalog.of(merchant);
        if (entry.getCurrency() != FiatCurrency.RUB) {
            throw new UnsupportedCurrencyException(merchant, entry.getPlatform(), entry.getCurrency());
        }
        return switch (entry.getPlatform()) {
            case ASGARD, BRIDGEPAY, CASHOUT, CROCOPAY, EVOPAY, GAMBIT, HONEYMONEY, LOTRIEN, PAYBOX,
                 PAYSYNC, PRISMAPAY, RSPAY, SMACKPAY -> "RUB";
            case FIATCUT, LEVELPAY -> "rub";
            default -> throw new UnsupportedCurrencyException(merchant, entry.getPlatform(), entry.getCurrency());
        };
    }

    /**
     * Числовой id валюты у площадки (viatrum: RUB = 1).
     */
    public static int numericId(Merchant merchant) {
        MerchantCatalog entry = MerchantCatalog.of(merchant);
        if (entry.getPlatform() == net.rcetech.md.meta.merchant.Platform.VIATRUM && entry.getCurrency() == FiatCurrency.RUB) {
            return 1;
        }
        throw new UnsupportedCurrencyException(merchant, entry.getPlatform(), entry.getCurrency());
    }

    /**
     * Проверка поддержки валюты для площадок, передающих валюту неявно
     * (cube: суффикс идентификатора метода).
     */
    public static void requireSupported(Merchant merchant) {
        MerchantCatalog entry = MerchantCatalog.of(merchant);
        if (entry.getCurrency() != FiatCurrency.RUB) {
            throw new UnsupportedCurrencyException(merchant, entry.getPlatform(), entry.getCurrency());
        }
    }
}
