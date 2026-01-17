package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {

    NATIONAL_PAYMENT_SYSTEM("СБП"),
    P2P_CARD("Карта"),
    ;

    private final String description;
}
