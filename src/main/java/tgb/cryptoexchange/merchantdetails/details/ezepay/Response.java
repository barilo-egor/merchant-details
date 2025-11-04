package tgb.cryptoexchange.merchantdetails.details.ezepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Data data;

    private String status;

    private String message;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (!"success".equals(status)) {
            result.addError("status", "expected success, got " + status + " with message \"" + message + "\"");
        } else {
            if (Objects.isNull(data)) {
                result.notNull("data");
            } else {
                if (Objects.isNull(data.getOrderId())) {
                    result.notNull("data.orderId");
                }
                if (Objects.isNull(data.getBank()) && Objects.isNull(data.getBankSbp())) {
                    result.notNull("data.bank", "data.bankSbp");
                }
                if (Objects.isNull(data.getDetails())) {
                    result.notNull("data.details");
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @lombok.Data
    public static class Data {

        @JsonProperty("uniqkey")
        private String orderId;

        @JsonProperty("props_name")
        private String bank;

        @JsonProperty("props_sbp")
        private String bankSbp;

        @JsonProperty("props")
        private String details;
    }

    public boolean isValid() {
        return this.getStatus().equals("success")
                && Objects.nonNull(this.getData())
                && (Objects.nonNull(this.getData().getBank()) || Objects.nonNull(this.getData().getBankSbp()))
                && Objects.nonNull(this.getData().getDetails())
                && Objects.nonNull(this.getData().getOrderId());
    }
}
