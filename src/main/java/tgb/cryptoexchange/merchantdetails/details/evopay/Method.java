package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    BANK_CARD("Карта", RequestMethod.CARD),
    SBP("СБП", RequestMethod.SBP);

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
