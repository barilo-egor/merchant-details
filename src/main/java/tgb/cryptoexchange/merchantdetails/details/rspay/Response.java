package tgb.cryptoexchange.merchantdetails.details.rspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("merchant_transaction_id")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Requisites requisites;

    @JsonProperty("payment_method")
    @JsonDeserialize(using = Method.Deserializer.class)
    private Method paymentMethod;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(id)) {
                result.notNull("id");
            }
            if (Objects.isNull(status)) {
                result.notNull("status");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(requisites) && (Objects.nonNull(requisites.getPaymentLink()) ||
                (Objects.nonNull(requisites.getBankName()) && Objects.nonNull(requisites.getPhoneNumber()) || Objects.nonNull(requisites.getCardNumber()))
                || (Objects.nonNull(requisites.getMobileProvider()) && Objects.nonNull(requisites.getPhoneNumber())));
    }

    @Data
    public static class Requisites {

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("phone_number")
        private String phoneNumber;

        @JsonProperty("payment_link")
        private String paymentLink;

        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("mobile_provider")
        private String mobileProvider;
    }

}
