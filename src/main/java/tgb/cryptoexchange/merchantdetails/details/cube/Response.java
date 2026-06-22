package tgb.cryptoexchange.merchantdetails.details.cube;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(data.internalId)) {
                result.notNull("data.internalId");
            }
            if (Objects.isNull(data.status)) {
                result.notNull("data.status");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.bankName) && Objects.nonNull(data.receiver);
    }

    @lombok.Data
    public static class Data {

        @JsonProperty("internal_id")
        private String internalId;

        @JsonProperty("bank_name")
        private String bankName;

        private String receiver;

        private Status status;

    }
}