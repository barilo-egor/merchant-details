package tgb.cryptoexchange.merchantdetails.details.noros;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private String bankReceiver;

    private String card;

    private Integer amount;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(bankReceiver)) {
            result.notNull("bankReceiver");
        }
        if (Objects.isNull(card)) {
            result.notNull("card");
        }
        if (Objects.isNull(amount)) {
            result.notNull("amount");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
