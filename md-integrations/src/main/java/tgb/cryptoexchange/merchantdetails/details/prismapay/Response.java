package tgb.cryptoexchange.merchantdetails.details.prismapay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements MerchantDetailsResponse {

    private String status;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!"success".equalsIgnoreCase(status)) {
            result.addError("status", String.format("expected 'success' but was %s", status));
            return result;
        }
        if (hasDetails()) {
            if (Objects.isNull(data.id)) {
                result.notNull("data.id");
            }
            if (Objects.isNull(data.amount)) {
                result.notNull("data.amount");
            }
            if (Objects.isNull(data.status)) {
                result.notNull("data.status");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.paymentBank) && Objects.nonNull(data.paymentDetails);
    }

    @lombok.Data
    public static class Data {

        private String id;

        @JsonProperty("amountIn")
        private Integer amount;

        private Status status;

        private String paymentBank;

        private String paymentDetails;
    }

}
