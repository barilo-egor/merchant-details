package tgb.cryptoexchange.merchantdetails.details;

import java.util.Optional;

public interface MerchantCallback {

    Optional<String> getMerchantOrderId();

    Optional<String> getStatus();

    Optional<String> getStatusDescription();
}
