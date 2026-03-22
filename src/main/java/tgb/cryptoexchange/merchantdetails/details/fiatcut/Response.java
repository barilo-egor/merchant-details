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

    @JsonProperty("order_id")
    private String orderId;

    private String amount;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("payment_gateway_name")
    private String bankName;

    @JsonProperty("payment_detail")
    private PaymentDetail paymentDetail;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(orderId)) {
                result.notNull("orderId");
            }
            if (Objects.isNull(status)) {
                result.notNull("status");
            }
            if (Objects.isNull(bankName)) {
                result.notNull("bankName");
            }
            if (StringUtils.isBlank(paymentDetail.getDetail())) {
                result.addError("paymentDetail.detail", "must be not blank");
            }
        } else {
            result.notNull("paymentDetail");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(paymentDetail) && Objects.nonNull(paymentDetail.getDetail());
    }

    @Data
    public static class PaymentDetail {

        private String detail;
    }
}
