package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HesoyamMethod implements Method {
    CARD("a23f88ed-08e7-404f-9d49-d9a4776eaa67", "Карта"),
    SBP("a23f88ed-0c7a-4797-b6f5-2f2f132aee59", "СБП"),
    QR("a23f88ed-0d41-495d-9aac-05ea3ed7a6fa", "QR");

    final String methodUid;

    final String description;

}
