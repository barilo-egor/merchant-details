package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Optional;

public interface MerchantCallback {

    Optional<String> getMerchantOrderId();

    Optional<String> getStatusName();

    Optional<String> getStatusDescription();

    @JsonIgnore
    default boolean isIgnored() {
        return false;
    }
}
