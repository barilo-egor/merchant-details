package tgb.cryptoexchange.merchantdetails.details.goatx;

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

    @JsonDeserialize(using = Method.Deserializer.class)
    private Method way;

    private Requisite requisite;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(way)) {
            result.notNull("way");
        }
        if (hasDetails()) {
            if (Objects.isNull(requisite.getCardNumber()) && Objects.isNull(requisite.getPhoneNumber())) {
                result.notNull("cardNumber", "phoneNumber");
            }
            if (Objects.isNull(requisite.getBank()) || Objects.isNull(requisite.getBank().getName())) {
                result.notNull("bank.name");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(requisite);
    }

    @Data
    public static class Requisite {

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("phone_number")
        private String phoneNumber;

        private Bank bank;

        @Data
        public static class Bank {

            private String name;
        }
    }
}
