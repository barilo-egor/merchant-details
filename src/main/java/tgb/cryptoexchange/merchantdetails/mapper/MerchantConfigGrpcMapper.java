package tgb.cryptoexchange.merchantdetails.mapper;

import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;

@Component
public class MerchantConfigGrpcMapper {

    public MerchantConfigDTOGrpc mapToGrpcDto(MerchantConfigDTO dto) {
        MerchantConfigDTOGrpc.Builder builder = MerchantConfigDTOGrpc.newBuilder()
                .setId(dto.getId())
                .setIsOn(dto.getIsOn() != null && dto.getIsOn())
                .setIsAutoWithdrawalOn(dto.getIsAutoWithdrawalOn() != null && dto.getIsAutoWithdrawalOn());

        if (dto.getMerchant() != null) {
            builder.setMerchant(dto.getMerchant().name());
        }
        if (dto.getMaxAmount() != null) {
            builder.setMaxAmount(Int32Value.of(dto.getMaxAmount()));
        }
        if (dto.getMinAmount() != null) {
            builder.setMinAmount(Int32Value.of(dto.getMinAmount()));
        }
        if (dto.getMerchantOrder() != null) {
            builder.setMerchantOrder(Int32Value.of(dto.getMerchantOrder()));
        }
        if (dto.getGroupChatId() != null) {
            builder.setGroupChatId(Int64Value.of(dto.getGroupChatId()));
        }
        if (dto.getStatuses() != null) {
            builder.addAllStatuses(dto.getStatuses().stream()
                    .map(s -> MerchantOrderStatusGrpc.newBuilder()
                            .setName(s.name())
                            .setDescription(s.getDescription())
                            .build())
                    .toList());
        }
        if (dto.getSuccessStatuses() != null) {
            builder.addAllSuccessStatuses(dto.getSuccessStatuses());
        }
        if (dto.getMethods() != null) {
            builder.addAllMethods(dto.getMethods().stream()
                    .map(m -> MerchantMethodGrpc.newBuilder()
                            .setName(m.name())
                            .setDescription(m.getDescription())
                            .build())
                    .toList());
        }
        if (dto.getConfirmConfigs() != null) {
            builder.addAllConfirmConfigs(dto.getConfirmConfigs().stream()
                    .map(c -> AutoConfirmConfigDTOGrpc.newBuilder()
                            .setCryptoCurrency(c.getCryptoCurrency().name())
                            .setAutoConfirmType(c.getAutoConfirmType().name())
                            .setDeliveryType(c.getDeliveryType().name())
                            .build())
                    .toList());
        }
        return builder.build();
    }

    public Pageable createPageable(PaginationGrpc pagination) {
        int page = Math.max(pagination.getPage(), 0);
        int size = pagination.getSize() > 0 ? pagination.getSize() : 20;
        return PageRequest.of(page, size, parseSort(pagination.getSort()));
    }

    private Sort parseSort(String sortString) {
        if (StringUtils.isBlank(sortString)) {
            return Sort.unsorted();
        }
        String[] parts = sortString.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

}
