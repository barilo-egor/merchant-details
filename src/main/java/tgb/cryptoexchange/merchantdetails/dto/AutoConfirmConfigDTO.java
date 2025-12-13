package tgb.cryptoexchange.merchantdetails.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;
import tgb.cryptoexchange.merchantdetails.entity.AutoConfirmConfig;

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
