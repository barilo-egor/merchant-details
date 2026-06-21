package tgb.cryptoexchange.merchantdetails.details.paysync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("trade")
    private String id;

    @JsonProperty("card_number")
    private String cardNumber;

    private String bank;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(bank) && Objects.nonNull(cardNumber);
    }
}
