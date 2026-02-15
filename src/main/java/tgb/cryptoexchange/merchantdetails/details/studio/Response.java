package tgb.cryptoexchange.merchantdetails.details.studio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements MerchantDetailsResponse {

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("internal_id")
    private String internalId;

    @JsonProperty("client_order_id")
    private String clientOrderId;

    private Integer amount;

    private Requisites requisites;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Requisites {

        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("card_number")
        private String cardNumber;

    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(internalId)) {
            result.notNull("internalId");
        }
        if (hasDetails()) {
            if (Objects.isNull(requisites.getBankName())) {
                result.notNull("bankName");
            }
            if (Objects.isNull(requisites.getCardNumber())) {
                result.notNull("cardNumber");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(requisites);
    }

}
