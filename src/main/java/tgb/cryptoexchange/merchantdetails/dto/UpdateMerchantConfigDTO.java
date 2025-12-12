package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Boolean isAutoWithdrawalOn;

    private List<String> successStatuses;

    private Integer maxAmount;

    private Integer minAmount;

    private Long groupChatId;
}
