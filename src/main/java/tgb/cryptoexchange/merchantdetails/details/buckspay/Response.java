package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String id;

    @JsonProperty("state")
    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("qrm_qr_link")
    private String qrLink;

    private Bank bank;

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
        return (Objects.nonNull(bank) && Objects.nonNull(bank.name) && Objects.nonNull(cardNumber) ||
                Objects.nonNull(phoneNumber)) || Objects.nonNull(qrLink);
    }

    @Data
    public static class Bank {

        private String name;

    }

}
