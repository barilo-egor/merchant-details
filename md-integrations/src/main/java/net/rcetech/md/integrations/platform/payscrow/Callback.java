package net.rcetech.md.integrations.platform.payscrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

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
    public Optional<String> getStatusName() {
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
