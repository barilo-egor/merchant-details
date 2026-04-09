package tgb.cryptoexchange.merchantdetails.details.viatrum;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Boolean success;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(success)) {
            result.notNull("success");
        } else if (Boolean.FALSE.equals(success)) {
            result.addError("success", "expected true but was false");
        } else {
            validateData(result);
        }
        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.isNull(data)) {
            result.notNull("data");
        } else {
            if (Objects.isNull(data.id)) {
                result.notNull("data.id");
            }
            if (Objects.isNull(data.status)) {
                result.notNull("data.status");
            }
            if (Objects.isNull(data.receiver)) {
                result.notNull("data.receiver");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.receiver) && (data.receiver.startsWith("https") ||
                Objects.nonNull(data.bank));
    }

    @lombok.Data
    public static class Data {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String bank;

        private String receiver;
    }
}
