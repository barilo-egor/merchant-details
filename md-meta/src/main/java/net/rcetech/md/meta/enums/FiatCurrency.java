package net.rcetech.md.meta.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Фиатные валюты мерчантов. Лимиты по умолчанию — масштаб валюты,
 * применяются при создании новых конфигов вместо рублёвых литералов 1..5000.
 * Имена значений совпадают с enum FiatCurrency в rce-meta.
 */
@Getter
@AllArgsConstructor
public enum FiatCurrency {

    RUB(5000, 1),
    KZT(280000, 1);

    /** Лимит максимальной суммы по умолчанию. */
    private final int defaultMaxAmount;

    /** Лимит минимальной суммы по умолчанию. */
    private final int defaultMinAmount;
}
