package tgb.cryptoexchange.merchantdetails.details.gambit;

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

    @JsonProperty("merchant_order_id")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Double amount;

    @JsonProperty("payment_details")
    private Requisites paymentDetails;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(amount)) {
            result.notNull("amount");
        }

        if (hasDetails()) {
            if (Objects.isNull(paymentDetails.getPhone()) && Objects.isNull(paymentDetails.getCardNumber())) {
                result.notNull("phone", "cardNumber");
            }
            if (Objects.nonNull(paymentDetails.getPhone())) {
                if (Objects.isNull(paymentDetails.getBankName()) && Objects.isNull(paymentDetails.getOperator())) {
                    result.notNull("bankName", "operator");
                }
            } else if (Objects.nonNull(paymentDetails.getCardNumber())) {
                if (Objects.isNull(paymentDetails.getBankName())) {
                    result.notNull("bankName");
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(paymentDetails);
    }

    @Data
    public static class Requisites {

        private String phone;

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("bank_name")
        private String bankName;

        private String operator;
    }
}
