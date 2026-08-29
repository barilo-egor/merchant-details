package tgb.cryptoexchange.merchantdetails.details.tronex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String status;

    private Data platform;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!"ok".equalsIgnoreCase(status)) {
            result.addError("status", String.format("expected 'ok' but was %s", status));
            return result;
        }
        validateData(result);

        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.nonNull(platform)) {
            if (Objects.isNull(platform.id)) {
                result.notNull("platform.id");
            }
            if (Objects.isNull(platform.amount)) {
                result.notNull("platform.amount");
            }
            if (Objects.isNull(platform.method)) {
                result.notNull("platform.method");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(platform) &&
                Objects.nonNull(platform.getBank()) &&
                (Objects.nonNull(platform.getNumber()) || Objects.nonNull(platform.paymentUrl));
    }

    @lombok.Data
    public static class Data {

        @JsonProperty("trade_id")
        private String id;

        @JsonProperty("paymethod")
        @JsonDeserialize(using = Method.Deserializer.class)
        private Method method;

        private String number;

        private String amount;

        private String bank;

        @JsonProperty("payment_url")
        private String paymentUrl;
    }
}
