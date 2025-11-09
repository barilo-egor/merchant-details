package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Method {
    BANK_CARD("Карта"),
    SBP("СБП");

    final String description;
}
