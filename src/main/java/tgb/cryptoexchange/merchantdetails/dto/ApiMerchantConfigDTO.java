package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Merchant merchant;

    private Integer maxAmount;

    private Integer minAmount;

    private Integer merchantOrder;

    public static ApiMerchantConfigDTO fromEntity(ApiMerchantConfig merchantConfig) {
        ApiMerchantConfigDTO merchantConfigDTO = new ApiMerchantConfigDTO();
        merchantConfigDTO.setId(merchantConfig.getId());
        merchantConfigDTO.setIsOn(merchantConfig.getIsOn());
        merchantConfigDTO.setMerchant(merchantConfig.getMerchant());
        merchantConfigDTO.setMaxAmount(merchantConfig.getMaxAmount());
        merchantConfigDTO.setMinAmount(merchantConfig.getMinAmount());
        merchantConfigDTO.setMerchantOrder(merchantConfig.getMerchantOrder());
        return merchantConfigDTO;
    }

}
