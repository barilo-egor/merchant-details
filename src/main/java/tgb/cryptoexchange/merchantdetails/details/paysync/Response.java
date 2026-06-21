package tgb.cryptoexchange.merchantdetails.details.paysync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("ok")
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
            if (Objects.isNull(data.invoiceId)) {
                result.notNull("data.invoiceId");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.bank) && (Objects.nonNull(data.card) || Objects.nonNull(data.phone));
    }

    @lombok.Data
    public static class Data {

        private String invoiceId;

        private String bank;

        private String card;

        private String phone;
    }
}
