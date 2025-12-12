package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;

import java.util.List;
import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Merchant merchant;

    private Boolean isAutoWithdrawalOn;

    private List<MerchantOrderStatus> statuses;

    private List<String> successStatuses;

    private List<MerchantMethod> methods;

    private Integer maxAmount;

    private Integer minAmount;

    private Integer merchantOrder;

    private List<AutoConfirmConfigDTO> confirmConfigs;

    private Long groupChatId;

    public static MerchantConfigDTO fromEntity(MerchantConfig merchantConfig) {
        MerchantConfigDTO merchantConfigDTO = new MerchantConfigDTO();
        merchantConfigDTO.setId(merchantConfig.getId());
        merchantConfigDTO.setIsOn(merchantConfig.getIsOn());
        merchantConfigDTO.setMerchant(merchantConfig.getMerchant());
        merchantConfigDTO.setIsAutoWithdrawalOn(merchantConfig.getIsAutoWithdrawalOn());
        merchantConfigDTO.setStatuses(merchantConfig.getMerchant().getStatuses());
        if (Objects.nonNull(merchantConfig.getSuccessStatuses())) {
            merchantConfigDTO.setSuccessStatuses(merchantConfig.getSuccessStatuses().stream()
                    .map(MerchantSuccessStatus::getStatus)
                    .toList());
        }
        merchantConfigDTO.setMethods(merchantConfig.getMerchant().getMethods());
        merchantConfigDTO.setMaxAmount(merchantConfig.getMaxAmount());
        merchantConfigDTO.setMinAmount(merchantConfig.getMinAmount());
        merchantConfigDTO.setMerchantOrder(merchantConfig.getMerchantOrder());
        if (Objects.nonNull(merchantConfig.getConfirmConfigs())) {
            merchantConfigDTO.setConfirmConfigs(
                    merchantConfig.getConfirmConfigs().stream().map(AutoConfirmConfigDTO::fromEntity).toList()
            );
        }
        merchantConfigDTO.setGroupChatId(merchantConfig.getGroupChatId());
        return merchantConfigDTO;
    }
}
