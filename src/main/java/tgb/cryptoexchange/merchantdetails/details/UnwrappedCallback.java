package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.Optional;

public abstract class UnwrappedCallback implements MerchantCallback {

    public abstract String getId();

    public abstract MerchantOrderStatus getStatus();

    @Override
    @JsonIgnore
    public Optional<String> getMerchantOrderId() {
        return Optional.ofNullable(getId());
    }

    @Override
    @JsonIgnore
    public Optional<String> getStatusName() {
        MerchantOrderStatus status = getStatus();
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.ofNullable(status.name());
    }

    @Override
    @JsonIgnore
    public Optional<String> getStatusDescription() {
        MerchantOrderStatus status = getStatus();
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.ofNullable(status.getDescription());
    }
}
