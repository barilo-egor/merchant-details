package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequest {

    private Long id;

    @NotBlank
    private String method;

    @NotNull
    @Min(1)
    private Integer amount;

    private Long chatId;

    private String initiatorApp;
}
