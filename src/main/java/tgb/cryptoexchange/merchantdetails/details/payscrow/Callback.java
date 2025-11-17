package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback implements MerchantCallback {

    private Payload payload;

    @Override
    public Optional<String> getMerchantOrderId() {
        if (Objects.nonNull(payload)) {
            return Optional.ofNullable(payload.getId());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatus() {
        if (Objects.nonNull(payload) && Objects.nonNull(payload.getStatus())) {
            return Optional.of(payload.getStatus().name());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.nonNull(payload) && Objects.nonNull(payload.getStatus())) {
            return Optional.of(payload.getStatus().getDescription());
        }
        return Optional.empty();
    }

    @Data
    public static class Payload {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
