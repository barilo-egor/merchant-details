package tgb.cryptoexchange.merchantdetails.details.settlex;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Method {
    SBP("СБП"),
    C2C("Карта");

    private final String description;
}
