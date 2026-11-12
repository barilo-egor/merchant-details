package net.rcetech.md.integrations.platform.paylee;

import lombok.Data;
import net.rcetech.md.integrations.platform.MerchantDetailsResponse;
import net.rcetech.md.integrations.platform.ValidationResult;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Integer id;

    private String requisites;

    private String bankName;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonDeserialize(using = Method.Deserializer.class)
    private List<Method> requisitesType;

    private Double  price;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(requisites)) {
            result.notNull("requisites");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
