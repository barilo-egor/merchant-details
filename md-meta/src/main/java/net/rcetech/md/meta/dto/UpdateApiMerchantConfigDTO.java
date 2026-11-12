package net.rcetech.md.meta.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateApiMerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Integer maxAmount;

    private Integer minAmount;

    private Integer newOrder;

    private Boolean swap;

}
