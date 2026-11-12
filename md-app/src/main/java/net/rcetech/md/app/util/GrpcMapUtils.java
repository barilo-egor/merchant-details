package net.rcetech.md.app.util;

import org.apache.commons.lang3.StringUtils;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;
import net.rcetech.md.app.grpc.generated.AutoConfirmConfigDTOGrpc;
import net.rcetech.md.app.grpc.generated.MerchantConfigRequestGrpc;
import net.rcetech.md.meta.constants.AutoConfirmType;
import net.rcetech.md.meta.dto.AutoConfirmConfigDTO;
import net.rcetech.md.meta.dto.MerchantConfigFilter;

public class GrpcMapUtils {

    public static AutoConfirmConfigDTO mapToAutoConfirmConfigDTO(AutoConfirmConfigDTOGrpc configDTOGrpc) {
        AutoConfirmConfigDTO.AutoConfirmConfigDTOBuilder builder = AutoConfirmConfigDTO.builder();
        if (StringUtils.isNotBlank(configDTOGrpc.getCryptoCurrency())) {
            builder.cryptoCurrency(CryptoCurrency.valueOf(configDTOGrpc.getCryptoCurrency()));
        }
        if (StringUtils.isNotBlank(configDTOGrpc.getAutoConfirmType())) {
            builder.autoConfirmType(AutoConfirmType.valueOf(configDTOGrpc.getAutoConfirmType()));
        }
        if (StringUtils.isNotBlank(configDTOGrpc.getDeliveryType())) {
            builder.deliveryType(DeliveryType.valueOf(configDTOGrpc.getDeliveryType()));
        }
        return builder.build();
    }

    public static MerchantConfigFilter mapToMerchantConfigFilter(MerchantConfigRequestGrpc grpcRequest) {
        return new MerchantConfigFilter(
                grpcRequest.getMerchantsList().isEmpty()
                        ? null
                        : grpcRequest.getMerchantsList().stream().map(Merchant::valueOf).toList(),
                grpcRequest.hasMerchantOrder() ? grpcRequest.getMerchantOrder().getValue() : null
        );
    }

}
