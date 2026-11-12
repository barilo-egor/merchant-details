package net.rcetech.md.app.util;

import org.apache.commons.lang3.StringUtils;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;
import net.rcetech.md.app.grpc.generated.AutoConfirmConfigDTOGrpc;
import net.rcetech.md.app.grpc.generated.MerchantConfigRequestGrpc;
import net.rcetech.md.meta.constants.AutoConfirmType;
import net.rcetech.md.app.dto.AutoConfirmConfigDTO;
import net.rcetech.md.app.dto.MerchantConfigRequest;

public class GrpcMapUtils {

    public static AutoConfirmConfigDTO mapToAutoConfirmConfigDTO(AutoConfirmConfigDTOGrpc configDTOGrpc) {
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
        if (grpcRequest.hasMerchantOrder()) {
            request.setMerchantOrder(grpcRequest.getMerchantOrder().getValue());
        }
        request.setSort(grpcRequest.getSort());
        if (!grpcRequest.getMerchantsList().isEmpty()) {
            request.setMerchants(grpcRequest.getMerchantsList().stream().map(Merchant::valueOf).toList());
        }
        return request;
    }

}
