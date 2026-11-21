package net.rcetech.md.app.mapper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import org.springframework.stereotype.Component;
import net.rcetech.md.app.grpc.generated.ApiMerchantConfigItemGrpc;
import net.rcetech.md.app.grpc.generated.UpdateApiMerchantConfigItemGrpc;
import net.rcetech.md.meta.dto.ApiMerchantConfigDTO;
import net.rcetech.md.meta.dto.UpdateApiMerchantConfigDTO;

@Component
public class ApiMerchantConfigGrpcMapper {

    public ApiMerchantConfigItemGrpc mapToGrpcApiMerchantConfigItem(ApiMerchantConfigDTO dto) {
        ApiMerchantConfigItemGrpc.Builder builder = ApiMerchantConfigItemGrpc.newBuilder()
                .setId(dto.id())
                .setIsOn(BoolValue.of(dto.isOn()));

        if (dto.merchant() != null) {
            builder.setMerchant(dto.merchant().name());
        }
        if (dto.maxAmount() != null) {
            builder.setMaxAmount(Int32Value.of(dto.maxAmount()));
        }
        if (dto.minAmount() != null) {
            builder.setMinAmount(Int32Value.of(dto.minAmount()));
        }
        if (dto.merchantOrder() != null) {
            builder.setMerchantOrder(Int32Value.of(dto.merchantOrder()));
        }
        return builder.build();
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
        if (grpcRequest.hasNewOrder()) {
            request.setNewOrder(grpcRequest.getNewOrder().getValue());
        }
        if (grpcRequest.hasSwap()) {
            request.setSwap(grpcRequest.getSwap().getValue());
        }
        return request;
    }

}
