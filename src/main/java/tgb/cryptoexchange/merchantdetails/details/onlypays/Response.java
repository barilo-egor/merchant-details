package tgb.cryptoexchange.merchantdetails.details.onlypays;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private boolean success;

    private String error;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!success) {
            result.addError("success", "expected true, but was false: " + error);
        } else {
            if (Objects.nonNull(data)) {
                if (Objects.isNull(data.getId())) {
                    result.notNull("data.id");
                }
                if (Objects.isNull(data.getRequisite())) {
                    result.notNull("data.requisite");
                }
                if (Objects.isNull(data.getBank())) {
                    result.notNull("data.bank");
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data);
    }

    @lombok.Data
    public static class Data {

        private String id;

        private String requisite;

        private String bank;
    }
}
