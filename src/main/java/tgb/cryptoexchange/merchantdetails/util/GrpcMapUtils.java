package tgb.cryptoexchange.merchantdetails.util;

import org.apache.commons.lang3.StringUtils;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.grpc.generated.AutoConfirmConfigDTOGrpc;
import tgb.cryptoexchange.grpc.generated.MerchantConfigRequestGrpc;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;
import tgb.cryptoexchange.merchantdetails.dto.AutoConfirmConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;

public class GrpcMapUtils {

    public static AutoConfirmConfigDTO mapToAutoConfirmConfigDTO(AutoConfirmConfigDTOGrpc configDTOGrpc){
        AutoConfirmConfigDTO configDTO = new AutoConfirmConfigDTO();

        if (StringUtils.isNotBlank(configDTOGrpc.getCryptoCurrency())) {
            configDTO.setCryptoCurrency(CryptoCurrency.valueOf(configDTOGrpc.getCryptoCurrency()));
        }

        if (StringUtils.isNotBlank(configDTOGrpc.getAutoConfirmType())) {
            configDTO.setAutoConfirmType(AutoConfirmType.valueOf(configDTOGrpc.getAutoConfirmType()));
        }

        if (StringUtils.isNotBlank(configDTOGrpc.getDeliveryType())) {
            configDTO.setDeliveryType(DeliveryType.valueOf(configDTOGrpc.getDeliveryType()));
        }

        return configDTO;
    }

    public static MerchantConfigRequest mapToMerchantConfigRequest(MerchantConfigRequestGrpc grpcRequest) {
        MerchantConfigRequest request = new MerchantConfigRequest();
        if (StringUtils.isNotBlank(grpcRequest.getMerchant())) {
            request.setMerchant(Merchant.valueOf(grpcRequest.getMerchant()));
        }

        if (grpcRequest.hasMerchantOrder()) {
            request.setMerchantOrder(grpcRequest.getMerchantOrder().getValue());
        }
        request.setSort(grpcRequest.getSort());
        return request;
    }

}
