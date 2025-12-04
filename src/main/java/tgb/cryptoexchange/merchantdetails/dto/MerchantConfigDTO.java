package tgb.cryptoexchange.merchantdetails.dto;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.List;

@Data
public class MerchantConfigDTO {

    private Boolean isOn;

    private Merchant merchant;

    private Boolean isAutoWithdrawalOn;

    private List<MerchantSuccessStatusDTO> successStatuses;

    private Integer maxAmount;

    private Integer minAmount;

    private Integer merchantOrder;

    private List<AutoConfirmConfigDTO> confirmConfigs;

    private Long groupChatId;

    public static MerchantConfigDTO fromEntity(MerchantConfig merchantConfig) {
        MerchantConfigDTO merchantConfigDTO = new MerchantConfigDTO();
        merchantConfigDTO.setIsOn(merchantConfig.getIsOn());
        merchantConfigDTO.setMerchant(merchantConfig.getMerchant());
        merchantConfigDTO.setIsAutoWithdrawalOn(merchantConfig.getIsAutoWithdrawalOn());
        merchantConfigDTO.setSuccessStatuses(
                merchantConfig.getSuccessStatuses().stream().map(MerchantSuccessStatusDTO::fromEntity).toList()
        );
        merchantConfigDTO.setMaxAmount(merchantConfig.getMaxAmount());
        merchantConfigDTO.setMinAmount(merchantConfig.getMinAmount());
        merchantConfigDTO.setMerchantOrder(merchantConfig.getMerchantOrder());
        merchantConfigDTO.setConfirmConfigs(
                merchantConfig.getConfirmConfigs().stream().map(AutoConfirmConfigDTO::fromEntity).toList()
        );
        merchantConfigDTO.setGroupChatId(merchantConfig.getGroupChatId());
        return merchantConfigDTO;
    }
}
