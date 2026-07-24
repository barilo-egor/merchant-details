package tgb.cryptoexchange.merchantdetails.details.zpay;

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

    @JsonProperty("data")
    private CallbackData data;

    @Override
    public Optional<String> getMerchantOrderId() {
        if (Objects.nonNull(data)) {
            return Optional.ofNullable(data.getId());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatusName() {
        if (Objects.nonNull(data) && Objects.nonNull(data.getStatus())) {
            return Optional.of(data.getStatus().name());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.nonNull(data) && Objects.nonNull(data.getStatus())) {
            return Optional.of(data.getStatus().getDescription());
        }
        return Optional.empty();
    }

    @Data
    public static class CallbackData {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}