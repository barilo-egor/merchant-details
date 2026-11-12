package net.rcetech.md.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;
import net.rcetech.md.meta.constants.AutoConfirmType;
import net.rcetech.md.domain.entity.AutoConfirmConfig;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoConfirmConfigDTO {

    private CryptoCurrency cryptoCurrency;

    private AutoConfirmType autoConfirmType;

    private DeliveryType deliveryType;

    public static AutoConfirmConfigDTO fromEntity(AutoConfirmConfig autoConfirmConfig) {
        AutoConfirmConfigDTO dto = new AutoConfirmConfigDTO();
        dto.setCryptoCurrency(autoConfirmConfig.getCryptoCurrency());
        dto.setAutoConfirmType(autoConfirmConfig.getAutoConfirmType());
        dto.setDeliveryType(autoConfirmConfig.getDeliveryType());
        return dto;
    }
}
