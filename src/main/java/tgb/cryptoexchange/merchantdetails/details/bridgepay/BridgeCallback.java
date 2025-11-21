package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

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
    public Optional<String> getStatus() {
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
