package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    TO_CARD("Карта"),
    SBP("СБП");

    private final String description;
}
