package tgb.cryptoexchange.merchantdetails.details.asgard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements MerchantDetailsResponse {

    @JsonProperty("result")
    private Requisites requisites;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        if (hasDetails()) {
            if (Objects.isNull(requisites.getId())) {
                result.notNull("id");
            }
            if (Objects.isNull(requisites.getAmount())) {
                result.notNull("amount");
            }
            if (Objects.isNull(requisites.getState())) {
                result.notNull("state");
            }
            if (Objects.isNull(requisites.getMethod())) {
                result.notNull("method");
            }
            if (Objects.isNull(requisites.getBankName())) {
                result.notNull("bankName");
            }
            if (Objects.isNull(requisites.getAddress())) {
                result.notNull("address");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(requisites);
    }

    @Data
    public static class Requisites {

        private String id;

        private Double amount;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status state;

        @JsonDeserialize(using = Method.Deserializer.class)
        private Method method;

        private String bankName;

        private String address;
    }
}
