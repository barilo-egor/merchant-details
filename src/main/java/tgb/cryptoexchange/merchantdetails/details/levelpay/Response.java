package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private boolean success;

    private Order data;

    @Data
    public static class Order {

        @JsonProperty("order_id")
        private String orderId;

        private String amount;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        @JsonProperty("payment_gateway_name")
        private String paymentGatewayName;

        @JsonProperty("payment_detail")
        private PaymentDetail paymentDetail;

        @Data
        public static class PaymentDetail {

            private String detail;
        }
    }
}
