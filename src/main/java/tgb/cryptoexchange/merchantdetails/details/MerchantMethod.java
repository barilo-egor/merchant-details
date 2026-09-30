package tgb.cryptoexchange.merchantdetails.details;

import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

public interface MerchantMethod {

    String name();

    String getDescription();

    Optional<RequestMethod> getRequestMethod();
}
