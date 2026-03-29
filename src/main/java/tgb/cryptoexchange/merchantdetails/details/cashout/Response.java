package tgb.cryptoexchange.merchantdetails.details.cashout;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String success;

    private ResponseRequisite data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(data)) {
            result.notNull("data");
        }
        if (Objects.nonNull(data) && Objects.isNull(data.getTransactionId())) {
            result.notNull("data.transactionId");
        }
        if (Objects.nonNull(data) && Objects.isNull(data.getAmount())) {
            result.notNull("data.amount");
        }

        return result;
    }

    @Override
    public boolean hasDetails() {
        boolean result = Objects.nonNull(data.getPaymentDetails());
        if (result) {
            PaymentDetails paymentDetails = data.getPaymentDetails();
            return Objects.nonNull(paymentDetails.getBankName()) && Objects.nonNull(paymentDetails.getCardNumber());
        }
        return false;
    }

    @Data
    public static class ResponseRequisite {

        private String transactionId;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String amount;

        private PaymentDetails paymentDetails;

    }

    @Data
    public static class PaymentDetails {

        private String cardNumber;

        private String bankName;

    }

}
