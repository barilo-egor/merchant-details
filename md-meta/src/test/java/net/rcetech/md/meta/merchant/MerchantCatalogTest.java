package net.rcetech.md.meta.merchant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Инвариант справочника: на каждое значение Merchant из commons есть запись,
 * у записи заполнены площадка, валюта и префикс конфига.
 */
class MerchantCatalogTest {

    @ParameterizedTest
    @EnumSource(Merchant.class)
    void everyMerchantHasCatalogEntry(Merchant merchant) {
        MerchantCatalog entry = MerchantCatalog.of(merchant);
        assertEquals(merchant.name(), entry.name());
        assertNotNull(entry.getPlatform());
        assertNotNull(entry.getCurrency());
        assertFalse(entry.getConfigPrefix().isBlank());
    }

    @Test
    void catalogIsOneToOneWithMerchantEnum() {
        assertEquals(Merchant.values().length, MerchantCatalog.values().length,
                "Справочник должен покрывать enum Merchant 1:1 — заведи/удали записи MerchantCatalog вместе со значением commons");
    }
}
