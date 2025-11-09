package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Method {
    CARD("Карта", "/card"),
    SBP("СБП", "/sbp"),
    TRANSGRAN_SBP("Трансгран СБП", "transgran-sbp");

    final String displayName;

    final String uri;
}
