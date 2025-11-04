package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements MerchantDetailsResponse {

    private String id;

    private Status orderStatus;

    private Requisites requisites;

    private Method method;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(orderStatus)) {
            result.notNull("orderStatus");
        }
        if (Objects.isNull(requisites)) {
            result.notNull("requisites");
        }
        if (Objects.isNull(method)) {
            result.notNull("method");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @Data
    public static class Requisites {

        @JsonProperty("recipient_phone_number")
        private String recipientPhoneNumber;

        @JsonProperty("recipient_card_number")
        private String recipientCardNumber;

        @JsonProperty("recipient_bank")
        private String recipientBank;
    }
}
