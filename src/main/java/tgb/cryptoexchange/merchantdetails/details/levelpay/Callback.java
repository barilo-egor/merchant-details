package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback implements MerchantCallback {

    @JsonProperty("order_id")
    private String orderId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.ofNullable(orderId);
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
