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
    @JsonProperty("status")
    private Status responseStatus;

    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.of(orderId);
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
