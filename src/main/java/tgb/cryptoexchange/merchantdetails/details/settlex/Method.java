package tgb.cryptoexchange.merchantdetails.details.settlex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    SBP("СБП"),
    C2C("Карта");

    private final String description;
}
