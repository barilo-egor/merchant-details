package tgb.cryptoexchange.merchantdetails.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantConfigResponse extends ApiResponse<List<MerchantConfigDTO>> {

    public MerchantConfigResponse(List<MerchantConfigDTO> merchantConfigs) {
        super();
        super.setSuccess(true);
        super.setData(merchantConfigs);
    }
}
