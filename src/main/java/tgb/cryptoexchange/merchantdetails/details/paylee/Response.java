package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Integer id;

    private String requisites;

    private String bankName;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Double  price;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(requisites)) {
            result.notNull("requisites");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
