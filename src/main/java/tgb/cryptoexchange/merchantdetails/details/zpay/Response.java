package tgb.cryptoexchange.merchantdetails.details.zpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Integer id;

    @JsonProperty("bank_name")
    private String bankName;

    private String number;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(number)) {
            result.notNull("number");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

}
