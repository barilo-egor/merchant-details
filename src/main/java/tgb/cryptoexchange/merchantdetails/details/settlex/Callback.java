package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback implements MerchantCallback {

    private String internalId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status responseStatus;

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.ofNullable(internalId);
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
