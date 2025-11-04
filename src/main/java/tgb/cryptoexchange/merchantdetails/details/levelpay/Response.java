package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private boolean success;

    private Order data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (success) {
            if (Objects.isNull(data)) {
                result.notNull("data");
            } else {
                validateData(result);
            }
        } else {
            result.addError("success", "expected true but was false");
        }
        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.isNull(data.getOrderId())) {
            result.notNull("data.orderId");
        }
        if (Objects.isNull(data.getStatus())) {
            result.notNull("data.status");
        }
        if (Objects.isNull(data.getPaymentGatewayName())) {
            result.notNull("data.paymentGatewayName");
        }
        if (Objects.isNull(data.getPaymentDetail())) {
            result.notNull("data.paymentDetail");
        } else {
            if (Objects.isNull(data.getPaymentDetail().getDetail())) {
                result.notNull("data.paymentDetail.detail");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

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
