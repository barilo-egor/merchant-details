package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class ResponseTransgran implements MerchantDetailsResponse {

    private Boolean success;

    @JsonProperty("form_data")
    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!Boolean.TRUE.equals(success)) {
            result.addError("success", String.format("expected 'true' but was %s", success));
            return result;
        }
        validateData(result);

        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.isNull(data.orderData.orderId)) {
            result.notNull("data.orderData.orderId");
        }
        if (Objects.isNull(data.orderData.amount)) {
            result.notNull("data.orderData.amount");
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.getFormUrl()) && Objects.nonNull(data.getOrderData());
    }

    @lombok.Data
    public static class Data {

        @JsonProperty("form_url")
        private String formUrl;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        @JsonProperty("order_data")
        private OrderData orderData;

        @JsonProperty("redirect_url")
        private String redirectUrl;

        @lombok.Data
        public static class OrderData {

            @JsonProperty("client_order_id")
            private String orderId;

            private Double amount;

        }
    }
}
