package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
public class Callback implements MerchantCallback {

    private Long id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @Override
    public Optional<String> getMerchantOrderId() {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return Optional.of(id.toString());
    }

    @Override
    public Optional<String> getStatus() {
        if (status == null) {
            return Optional.empty();
        }
        return Optional.of(status.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (status == null) {
            return Optional.empty();
        }
        return Optional.of(status.getDisplayName());
    }
}
