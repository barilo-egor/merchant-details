package net.rcetech.md.integrations.platform.honeymoney;

import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantDetailsResponse;
import net.rcetech.md.integrations.platform.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Integer id;

    private String cardNumber;

    private String bankName;

    private String phoneNumber;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(cardNumber) && Objects.isNull(phoneNumber)) {
            result.notNull("cardNumber", "phoneNumber");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
