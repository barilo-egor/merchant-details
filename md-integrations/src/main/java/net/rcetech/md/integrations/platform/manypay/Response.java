package net.rcetech.md.integrations.platform.manypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantDetailsResponse;
import net.rcetech.md.integrations.platform.ValidationResult;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String status;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!"ok".equalsIgnoreCase(status)) {
            result.addError("status", String.format("expected 'ok' but was %s", status));
            return result;
        }
        validateData(result);

        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.isNull(data)) {
            result.notNull("data");
        } else {
            if (Objects.isNull(data.orderId)) {
                result.notNull("data.orderId");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.paymentDetails) &&
                Objects.nonNull(data.paymentDetails.bankName) && Objects.nonNull(data.paymentDetails.method)
                && Objects.nonNull(data.paymentDetails.details);
    }

    @lombok.Data
    public static class Data {

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("payment_details")
        private Requisites paymentDetails;

        private Status status;
    }

    @lombok.Data
    public static class Requisites {

        @JsonDeserialize(using = Method.Deserializer.class)
        @JsonProperty("detail_type")
        private Method method;

        @JsonProperty("detail_number")
        private String details;

        @JsonProperty("bank_name")
        private String bankName;
    }
}
