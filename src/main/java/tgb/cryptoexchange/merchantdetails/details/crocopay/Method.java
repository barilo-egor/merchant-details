package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Method {
    TO_CARD("Карта"),
    SBP("СБП");

    private final String description;
}
