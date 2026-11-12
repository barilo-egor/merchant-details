package net.rcetech.md.integrations.platform.onlypays;

import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantDetailsResponse;
import net.rcetech.md.integrations.platform.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private boolean success;

    private String error;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(data.getId())) {
                result.notNull("data.id");
            }
            if (Objects.isNull(data.getRequisite())) {
                result.notNull("data.requisite");
            }
            if (Objects.isNull(data.getBank())) {
                result.notNull("data.bank");
            }
        } else if (Objects.nonNull(error) && !"No available requisites".equals(error)) {
            result.expectedEmpty("error", error);
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return success;
    }

    @lombok.Data
    public static class Data {

        private String id;

        private String requisite;

        private String bank;
    }
}
