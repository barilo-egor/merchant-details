package tgb.cryptoexchange.merchantdetails.details.daopayments;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

import java.util.Objects;
import java.util.Optional;

@Data
public class Callback implements MerchantCallback {

    private Transaction transaction;

    @Override
    public Optional<String> getMerchantOrderId() {
        if (Objects.isNull(transaction)) {
            return Optional.empty();
        }
        return Optional.of(transaction.id);
    }

    @Override
    public Optional<String> getStatusName() {
        if (Objects.isNull(transaction) || Objects.isNull(transaction.status)) {
            return Optional.empty();
        }
        return Optional.of(transaction.status.name());
    }

    @Override
    public Optional<String> getStatusDescription() {
        if (Objects.isNull(transaction) || Objects.isNull(transaction.status)) {
            return Optional.empty();
        }
        return Optional.of(transaction.status.getDescription());
    }

    @Data
    public static class Transaction {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
