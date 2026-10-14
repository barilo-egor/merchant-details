package tgb.cryptoexchange.merchantdetails.details.smackpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
public class Response implements MerchantDetailsResponse {

    private String id;

    private Integer amount;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("payment_url")
    private String paymentUrl;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(amount)) {
            result.notNull("amount");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(paymentUrl);
    }

}
