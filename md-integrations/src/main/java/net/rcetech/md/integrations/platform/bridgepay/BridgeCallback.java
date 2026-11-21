package net.rcetech.md.integrations.platform.bridgepay;

import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;
import java.util.Optional;

@Data
public class BridgeCallback implements MerchantCallback {

    private InvoiceDTO invoice;

    @Override
    public Optional<String> getMerchantOrderId() {
        if (Objects.isNull(invoice)) {
            return Optional.empty();
        }
        return Optional.of(invoice.id);
    }

    @Override
    public Optional<String> getStatusName() {
        if (Objects.isNull(invoice) || Objects.isNull(invoice.status)) {
            return Optional.empty();
        }
        return Optional.of(invoice.status.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.isNull(invoice) || Objects.isNull(invoice.status)) {
            return Optional.empty();
        }
        return Optional.of(invoice.status.getDescription());
    }

    @Data
    public static class InvoiceDTO {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
