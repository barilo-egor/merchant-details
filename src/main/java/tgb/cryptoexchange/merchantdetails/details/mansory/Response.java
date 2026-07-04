package tgb.cryptoexchange.merchantdetails.details.mansory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("payment_id")
    private String orderId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Integer amount;

    @JsonProperty("recipient")
    private Requisites requisites;

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
            if (Objects.isNull(amount)) {
                result.notNull("amount");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(requisites) && Objects.nonNull(requisites.bankName) && (Objects.nonNull(requisites.getCardNumber()) || Objects.nonNull(requisites.getPhone()));
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Requisites {

        @JsonProperty("bank_name")
        private String bankName;

        private String phone;

        @JsonProperty("card_number")
        private String cardNumber;

    }

}
