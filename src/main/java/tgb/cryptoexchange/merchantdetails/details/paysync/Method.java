package tgb.cryptoexchange.merchantdetails.details.paysync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("Card", "Карта"),
    SBP("Sbp", "СБП");

    final String value;

    final String description;

}
