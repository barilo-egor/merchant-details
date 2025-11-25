package tgb.cryptoexchange.merchantdetails.details;

import java.util.Objects;
import java.util.Optional;

public abstract class UnwrappedCallback implements MerchantCallback {

    public abstract String getId();

    public abstract MerchantOrderStatus getStatus();

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.of(getId());
    }

    @Override
    public Optional<String> getStatusName() {
        MerchantOrderStatus status = getStatus();
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.of(status.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        MerchantOrderStatus status = getStatus();
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.of(status.getDescription());
    }
}
