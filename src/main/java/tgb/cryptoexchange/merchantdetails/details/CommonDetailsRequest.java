package tgb.cryptoexchange.merchantdetails.details;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public abstract class CommonDetailsRequest {

    private Long id;

    @NotNull
    @Min(1)
    private Integer amount;

    private Long chatId;

    private String initiatorApp;
}
