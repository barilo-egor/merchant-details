package tgb.cryptoexchange.merchantdetails.details.ezepay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Method {
    SBP(2, "СБП"),
    CARD(55, "Карта РФ");

    private final int id;

    private final String description;
}
