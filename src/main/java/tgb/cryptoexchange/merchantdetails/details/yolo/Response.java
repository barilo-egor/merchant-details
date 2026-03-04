package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String orderId;

    private Double value;

    @JsonDeserialize(using = Bank.Deserializer.class)
    private String bankName;

    private String accountNumber;

    private String contactNumber;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails() && Objects.isNull(orderId)) {
            result.notNull("orderId");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(accountNumber) && Objects.isNull(contactNumber)) {
            result.notNull("accountNumber or contactNumber");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

}
