package tgb.cryptoexchange.merchantdetails.mapper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.grpc.generated.ApiMerchantConfigItemGrpc;
import tgb.cryptoexchange.grpc.generated.FindAllApiMerchantConfigsRequestGrpc;
import tgb.cryptoexchange.grpc.generated.UpdateApiMerchantConfigItemGrpc;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;

@Component
public class ApiMerchantConfigGrpcMapper {

    public ApiMerchantConfigItemGrpc mapToGrpcApiMerchantConfigItem(ApiMerchantConfigDTO dto) {
        ApiMerchantConfigItemGrpc.Builder builder = ApiMerchantConfigItemGrpc.newBuilder()
                .setId(dto.getId())
                .setIsOn(BoolValue.of(dto.getIsOn()));

        if (dto.getMerchant() != null) {
            builder.setMerchant(dto.getMerchant().name());
        }
        if (dto.getMaxAmount() != null) {
            builder.setMaxAmount(Int32Value.of(dto.getMaxAmount()));
        }
        if (dto.getMinAmount() != null) {
            builder.setMinAmount(Int32Value.of(dto.getMinAmount()));
        }
        return builder.build();
    }

    public MerchantConfigRequest mapToApiMerchantConfigRequest(FindAllApiMerchantConfigsRequestGrpc grpcRequest) {
        MerchantConfigRequest request = new MerchantConfigRequest();
        if (StringUtils.isNotBlank(grpcRequest.getOwnerId())) {
            request.setOwnerId(grpcRequest.getOwnerId());
        }
        return request;
    }

    public UpdateApiMerchantConfigDTO mapToUpdateMerchantConfigDTO(UpdateApiMerchantConfigItemGrpc grpcRequest) {
        UpdateApiMerchantConfigDTO request = new UpdateApiMerchantConfigDTO();
        request.setId(grpcRequest.getId());
        if (grpcRequest.hasIsOn()) {
            request.setIsOn(grpcRequest.getIsOn().getValue());
        }
        if (grpcRequest.hasMaxAmount()) {
            request.setMaxAmount(grpcRequest.getMaxAmount().getValue());
        }
        if (grpcRequest.hasMinAmount()) {
            request.setMinAmount(grpcRequest.getMinAmount().getValue());
        }
        return request;
    }

}
