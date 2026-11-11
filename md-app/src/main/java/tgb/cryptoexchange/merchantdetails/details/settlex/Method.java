package tgb.cryptoexchange.merchantdetails.details.settlex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    SBP("СБП", RequestMethod.SBP),
    C2C("Карта", RequestMethod.CARD);

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
