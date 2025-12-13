package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    BANK_CARD("Карта"),
    SBP("СБП");

    final String description;
}
