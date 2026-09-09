package tgb.cryptoexchange.merchantdetails.details.baypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Boolean status;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!Boolean.TRUE.equals(status)) {
            result.addError("status", String.format("expected 'true' but was %s", status));
            return result;
        }
        validateData(result);

        return result;
    }

    private void validateData(ValidationResult result) {
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

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.getPaymentDetail()) &&
                Objects.nonNull(data.getPaymentDetail().getBank()) &&
                Objects.nonNull(data.getPaymentDetail().getDetail());
    }

    @lombok.Data
    public static class Data {

        private String id;

        private String amount;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        @JsonProperty("payment_detail")
        private PaymentDetail paymentDetail;

        @lombok.Data
        public static class PaymentDetail {

            private String detail;

            private String bank;

        }
    }
}
