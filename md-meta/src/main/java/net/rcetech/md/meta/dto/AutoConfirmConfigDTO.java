package net.rcetech.md.meta.dto;

import lombok.Builder;
import net.rcetech.md.meta.constants.AutoConfirmType;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;

/**
 * Настройка автоподтверждения сделок мерчанта.
 */
@Builder
public record AutoConfirmConfigDTO(
        CryptoCurrency cryptoCurrency,
        AutoConfirmType autoConfirmType,
        DeliveryType deliveryType
) {
}
