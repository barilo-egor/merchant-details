package tgb.cryptoexchange.merchantdetails.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.grpc.server.service.GrpcService;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.constants.Metrics;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;
import tgb.cryptoexchange.merchantdetails.util.GrpcMapUtils;

import java.util.List;

import static tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService.STATUS;
import static tgb.cryptoexchange.merchantdetails.util.GrpcMapUtils.mapToMerchantConfigRequest;

@GrpcService
@Slf4j
public class MerchantDetailsControllerGrpc extends MerchantDetailsServiceGrpc.MerchantDetailsServiceImplBase {

    private final MerchantDetailsService merchantDetailsService;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    private final MeterRegistry meterRegistry;

    public MerchantDetailsControllerGrpc(MerchantDetailsService merchantDetailsService,
                                         MerchantConfigService merchantConfigService, VariableService variableService,
                                         MeterRegistry meterRegistry) {
        this.merchantDetailsService = merchantDetailsService;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void cancelOrder(CancelOrderRequestGrpc request, StreamObserver<Empty> responseObserver) {
        Merchant merchant = Merchant.valueOf(request.getMerchant());
        CancelOrderRequest domainRequest = new CancelOrderRequest();
        domainRequest.setOrderId(request.getOrderId());
        domainRequest.setMethod(request.getMethod());

        merchantDetailsService.cancelOrder(merchant, domainRequest);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getConfig(MerchantConfigRequestGrpc request,
                          StreamObserver<MerchantConfigResponseGrpc> responseObserver) {
        int pageNum = request.getPagination().getPage() > 0 ? request.getPagination().getPage() : 0;
        int pageSize = request.getPagination().getSize() > 0 ? request.getPagination().getSize() : 20;
        Sort sort = Sort.unsorted();
        String sortString = request.getPagination().getSort();
        if (StringUtils.isNotBlank(sortString)) {
            String[] parts = sortString.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            sort = Sort.by(direction, property);
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<MerchantConfigDTO> page = merchantConfigService.findAll(pageable, mapToMerchantConfigRequest(request));

        List<MerchantConfigDTOGrpc> grpcData = page.getContent().stream()
                .map(dto -> {
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
                    if (dto.getMinDealsCount() != null) {
                        builder.setMinDealsCount(dto.getMinDealsCount());
                    }
                    return builder.build();
                })
                .toList();

        MerchantConfigResponseGrpc response = MerchantConfigResponseGrpc.newBuilder()
                .setSuccess(true)
                .addAllData(grpcData)
                .setTotalCount(page.getTotalElements())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateConfig(UpdateMerchantConfigRequestGrpc request, StreamObserver<Empty> responseObserver) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(request.getId());
        for (String path : request.getUpdateMask().getPathsList()) {
            switch (path) {
                case "is_on":
                    dto.setIsOn(request.getIsOn().getValue());
                    break;
                case "is_auto_withdrawal_on":
                    dto.setIsAutoWithdrawalOn(request.getIsAutoWithdrawalOn().getValue());
                    break;
                case "success_statuses":
                    dto.setSuccessStatuses(request.getSuccessStatusesList());
                    break;
                case "max_amount":
                    dto.setMaxAmount(request.getMaxAmount().getValue());
                    break;
                case "min_amount":
                    dto.setMinAmount(request.getMinAmount().getValue());
                    break;
                case "group_chat_id":
                    dto.setGroupChatId(request.getGroupChatId().getValue());
                    break;
                case "confirm_configs":
                    dto.setConfirmConfigs(request.getConfirmConfigsList().stream()
                            .map(GrpcMapUtils::mapToAutoConfirmConfigDTO)
                            .toList());
                    break;
                case "min_deals_count":
                    dto.setMinDealsCount(request.getMinDealsCount());
                    break;
            }
        }

        merchantConfigService.update(dto);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllGroupChatIds(Empty request, StreamObserver<GroupChatIdsResponseGrpc> responseObserver) {
        List<Long> result = merchantConfigService.findAllGroupChatIds();
        GroupChatIdsResponseGrpc response = GroupChatIdsResponseGrpc.newBuilder()
                .addAllGroupChatIds(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteConfigField(DeleteConfigFieldRequestGrpc request, StreamObserver<Empty> responseObserver) {
        Long id = request.getId();
        String fieldName = request.getFieldName();

        merchantConfigService.deleteField(id, fieldName);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateOrder(UpdateOrderRequestGrpc request, StreamObserver<Empty> responseObserver) {
        Merchant merchantFirst = Merchant.valueOf(request.getMerchantFirst());
        if (request.hasMerchantSecond()) {
            Merchant merchantSecond = Merchant.valueOf(request.getMerchantSecond().getValue());
            merchantConfigService.changeOrder(merchantFirst, merchantSecond);
        } else if (request.hasNewOrder()) {
            merchantConfigService.changeOrder(merchantFirst, request.getNewOrder().getValue());
        }
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void merchantsByMinDealsCount(MerchantsByMinDealsCountRequestGrpc request, StreamObserver<MerchantsByMinDealsCountResponseGrpc> responseObserver) {
        List<Merchant> merchants = merchantConfigService.findMerchantsByMinDealsCount(request.getMinDealsCount());

        responseObserver.onNext(MerchantsByMinDealsCountResponseGrpc
                .newBuilder()
                .addAllMerchants(merchants.stream().map(Merchant::name).toList())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVariable(GetVariableRequestGrpc request, StreamObserver<VariableDTOGrpc> responseObserver) {
        VariableType type = VariableType.valueOf(request.getVariableType().name());

        var entity = variableService.findByType(type);

        VariableDTOGrpc response = VariableDTOGrpc.newBuilder()
                .setType(VariableTypeGrpc.valueOf(type.name()))
                .setValue(entity.getValue())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateVariable(UpdateVariableRequestGrpc request, StreamObserver<Empty> responseObserver) {
        VariableType type = VariableType.valueOf(request.getVariableType().name());
        String newValue = request.getValue();

        variableService.update(type, newValue);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendReceipt(SendReceiptRequestGrpc request, StreamObserver<Empty> responseObserver) {
        try {
            Merchant merchant = Merchant.valueOf(request.getMerchant());
            merchantDetailsService.sendReceipt(
                    merchant,
                    request.getOrderId(),
                    request.getReceiptData().toByteArray(),
                    request.getFileName()
            );

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка в gRPC методе sendReceipt: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Ошибка обработки чека: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void requisiteIssuanceFailure(RequisiteIssuanceFailureGrpc request,
                                         StreamObserver<RequisiteFailureResponseGrpc> responseObserver) {
        Counter counter = meterRegistry.find(Metrics.GET_DETAILS_RESULT)
                .tag(STATUS, "empty")
                .tag("initiatorApp", request.getInitiatorApp())
                .tag("date", request.getDate())
                .counter();
        int count = counter == null ? 0 : (int) counter.count();
        RequisiteFailureResponseGrpc response = RequisiteFailureResponseGrpc.newBuilder()
                .setCount(count)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
