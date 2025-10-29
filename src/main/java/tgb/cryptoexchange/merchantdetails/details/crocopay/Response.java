package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {

    @JsonProperty("response")
    private ResponseData responseData;

    @Data
    public static class ResponseData {

        private Transaction transaction;

        private PaymentRequisites paymentRequisites;

        @Data
        public static class PaymentRequisites {

            private String paymentMethod;

            private String requisites;
        }
    }
}
