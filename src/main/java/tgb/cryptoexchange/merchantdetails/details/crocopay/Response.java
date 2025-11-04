package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("response")
    private ResponseData responseData;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(responseData)) {
            result.notNull("responseData");
        } else {
            if (Objects.nonNull(responseData.getTransaction())) {
                if (Objects.isNull(responseData.getTransaction().getId())) {
                    result.notNull("responseData.transaction.id");
                }
                if (Objects.isNull(responseData.getTransaction().getStatus())) {
                    result.notNull("responseData.transaction.status");
                }
            }
            if (Objects.nonNull(responseData.getPaymentRequisites())
                    && Objects.isNull(responseData.getPaymentRequisites().getPaymentMethod())) {
                result.notNull("responseData.paymentRequisites.paymentMethod");
            }

        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @Data
    public static class ResponseData {

        private Transaction transaction;

        private PaymentRequisites paymentRequisites;

        @Data
        public static class PaymentRequisites {

            private String paymentMethod;

            private String requisites;
        }

        @Data
        public static class Transaction {

            private String id;

            @JsonDeserialize(using = Status.Deserializer.class)
            private Status status;
        }
    }
}
