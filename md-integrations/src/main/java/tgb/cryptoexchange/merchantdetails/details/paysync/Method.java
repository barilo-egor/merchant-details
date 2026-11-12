package tgb.cryptoexchange.merchantdetails.details.paysync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("Card", "Карта", RequestMethod.CARD);

    final String value;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
