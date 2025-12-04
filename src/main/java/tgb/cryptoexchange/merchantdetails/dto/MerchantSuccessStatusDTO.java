package tgb.cryptoexchange.merchantdetails.dto;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;

@Data
public class MerchantSuccessStatusDTO {

    private String status;

    public static MerchantSuccessStatusDTO fromEntity(MerchantSuccessStatus merchantSuccessStatus) {
        MerchantSuccessStatusDTO merchantSuccessStatusDTO = new MerchantSuccessStatusDTO();
        merchantSuccessStatusDTO.setStatus(merchantSuccessStatus.getStatus());
        return merchantSuccessStatusDTO;
    }
}
