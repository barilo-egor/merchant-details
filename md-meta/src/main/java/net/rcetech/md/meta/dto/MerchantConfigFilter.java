package net.rcetech.md.meta.dto;

import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;

/**
 * Фильтр поиска конфигов мерчантов: пустые поля не ограничивают выборку.
 */
public record MerchantConfigFilter(
        List<Merchant> merchants,
        Integer merchantOrder
) {
}
