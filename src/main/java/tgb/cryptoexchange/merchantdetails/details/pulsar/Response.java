package tgb.cryptoexchange.merchantdetails.details.pulsar;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private boolean status;

    private Result result;

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = new ValidationResult();
        if (status) {
            if (Objects.isNull(result)) {
                validationResult.notNull("result");
            } else {
                validateResult(validationResult);
            }
        } else {
            validationResult.addError("success", "expected true but was false");
        }
        return validationResult;
    }

    private void validateResult(ValidationResult validationResult) {
        if (Objects.isNull(result.id)) {
            validationResult.notNull("result.id");
        }
        if (Objects.isNull(result.state)) {
            validationResult.notNull("result.state");
        }
        if (Objects.isNull(result.address)) {
            validationResult.notNull("result.address");
        }
        if (Objects.isNull(result.bankName)) {
            validationResult.notNull("result.bankName");
        }
    }

    @Override
    public boolean hasDetails() {
        return status && Objects.nonNull(result);
    }

    @Data
    public static class Result {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status state;

        private String address;

        private String bankName;
    }
}
