package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
public class Callback implements MerchantCallback {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.ofNullable(id);
    }

    @Override
    public Optional<String> getStatusName() {
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.of(status.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        return Optional.of(status.getDescription());
    }
}
