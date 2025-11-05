package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("method_name")
    private String methodName;

    @JsonProperty("holder_account")
    private String holderAccount;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.nonNull(id)) {
            result.notNull("id");
        }
        if (Objects.nonNull(status)) {
            result.notNull("status");
        }
        if (Objects.nonNull(methodName)) {
            result.notNull("methodName");
        }
        if (Objects.nonNull(holderAccount)) {
            result.notNull("holderAccount");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
