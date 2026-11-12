package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tgb.cryptoexchange.commons.enums.Merchant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiMerchantConfigDTO(
        Long id,
        Boolean isOn,
        Merchant merchant,
        Integer maxAmount,
        Integer minAmount,
        Integer merchantOrder
) {
}
