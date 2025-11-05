package tgb.cryptoexchange.merchantdetails.details.pspware;

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

    private String bankName;

    private String card;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(card)) {
            result.notNull("card");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
