package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
public class Callback implements MerchantCallback {

    private String offerId;

    @JsonDeserialize(using = Status.Deserializer.class)
    @JsonProperty("status")
    private Status responseStatus;

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.of(offerId);
    }

    @Override
    public Optional<String> getStatus() {
        if (Objects.isNull(responseStatus)) {
            return Optional.empty();
        }
        return Optional.of(responseStatus.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.isNull(responseStatus)) {
            return Optional.empty();
        }
        return Optional.of(responseStatus.getDescription());
    }
}
