package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("Карта", null, "/v2/merchant/transactions"),
    SBP("СБП", null, "/v2/merchant/transactions/sbp"),
    CROSS_BORDER("Трансгран", null, "/v2/merchant/transactions/cross-border"),
    SBER_ACCOUNT("Сбер номер счета", "СберБанк", "/v2/merchant/transactions/account");

    private final String description;

    private final String bank;

    private final String uri;
}
