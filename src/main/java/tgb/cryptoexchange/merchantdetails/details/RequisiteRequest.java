package tgb.cryptoexchange.merchantdetails.details;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequisiteRequest {

    @NotBlank
    private String method;

    @NotNull
    @Min(1)
    private Integer amount;

    private String callbackUrl;
}
