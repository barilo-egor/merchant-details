package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("Карта", "/card"),
    SBP("СБП", "/sbp"),
    TRANSGRAN_SBP("Трансгран СБП", "transgran-sbp");

    final String description;

    final String uri;
}
