package tgb.cryptoexchange.merchantdetails.details.fiatcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Boolean success;

    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("order_id")
        private String orderId;

        private String amount;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        @JsonProperty("payment_gateway_name")
        private String bankName;

        @JsonProperty("payment_detail")
        private PaymentDetail paymentDetail;

        @lombok.Data
        public static class PaymentDetail {

            private String detail;
        }
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(data.getOrderId())) {
                result.notNull("orderId");
            }
            if (Objects.isNull(data.getStatus())) {
                result.notNull("status");
            }
            if (Objects.isNull(data.getBankName())) {
                result.notNull("bankName");
            }
            if (StringUtils.isBlank(data.getPaymentDetail().getDetail())) {
                result.addError("paymentDetail.detail", "must be not blank");
            }
        } else {
            result.notNull("paymentDetail");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data.getPaymentDetail()) && Objects.nonNull(data.getPaymentDetail().getDetail());
    }


}
