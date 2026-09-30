package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum HesoyamMethod implements Method {
    CARD("a23f88ed-08e7-404f-9d49-d9a4776eaa67", "Карта", RequestMethod.CARD),
    SBP("a23f88ed-0c7a-4797-b6f5-2f2f132aee59", "СБП", RequestMethod.SBP),
    QR("a23f88ed-0d41-495d-9aac-05ea3ed7a6fa", "QR", null);

    final String methodUid;

    final String description;

    final RequestMethod requestMethod;

    @Override
    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
