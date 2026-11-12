package tgb.cryptoexchange.merchantdetails.details.lotrien;

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

    private Status status;

    private Method paymentMethod;

    @JsonProperty("fiatSum")
    private String amount;

    private Requisites requisites;

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
            if (Objects.isNull(amount)) {
                result.notNull("amount");
            }
            if (Objects.isNull(paymentMethod)) {
                result.notNull("paymentMethod");
            }
            if (Objects.isNull(requisites.getPhoneNumber()) && Objects.isNull(requisites.getCardNumber())) {
                result.notNull("phoneNumber", "cardNumber");
            }
            if (Objects.isNull(requisites.getBank())) {
                result.notNull("bank");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        if (Objects.nonNull(requisites)) {
            return Objects.nonNull(requisites.getBank()) &&
                    (Objects.nonNull(requisites.getPhoneNumber()) || Objects.nonNull(requisites.getCardNumber()));
        }
        return false;
    }

    @Data
    public static class Requisites {

        private String phoneNumber;

        private String cardNumber;

        private String bank;
    }
}
