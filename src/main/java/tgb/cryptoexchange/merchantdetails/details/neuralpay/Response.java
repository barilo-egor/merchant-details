package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements MerchantDetailsResponse {

    private String amount;

    private String id;

    private ResponseRequisite requisite;

    private Status status;

    @Data
    public static class ResponseRequisite {

        private String requisite;

        private String bankName;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(amount)) {
            result.notNull("amount");
        }
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(requisite)) {
            result.notNull("requisite");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
