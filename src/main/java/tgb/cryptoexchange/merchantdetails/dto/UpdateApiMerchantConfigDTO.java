package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateApiMerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Integer maxAmount;

    private Integer minAmount;
}
