package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String id;

    private String orderId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Requisites requisites;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(orderId)) {
            result.notNull("orderId");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(requisites)) {
            result.notNull("requisites");
        } else {
            if (Objects.isNull(requisites.getBankName())) {
                result.notNull("requisites.bankName");
            }
            if (Objects.isNull(requisites.getCardNumber())) {
                result.notNull("requisites.cardNumber");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @Data
    public static class Requisites {

        private String bankName;

        private String cardNumber;
    }
}
