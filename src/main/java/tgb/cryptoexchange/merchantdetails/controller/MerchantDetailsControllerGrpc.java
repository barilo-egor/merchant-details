package tgb.cryptoexchange.merchantdetails.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.grpc.server.service.GrpcService;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.constants.Metrics;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;
import tgb.cryptoexchange.merchantdetails.mapper.MerchantConfigGrpcMapper;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;
import tgb.cryptoexchange.merchantdetails.util.GrpcMapUtils;

import java.util.List;

import static tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService.STATUS;
import static tgb.cryptoexchange.merchantdetails.util.GrpcMapUtils.mapToMerchantConfigRequest;

@Slf4j
@GrpcService
public class MerchantDetailsControllerGrpc extends MerchantDetailsServiceGrpc.MerchantDetailsServiceImplBase {

    private final MerchantDetailsService merchantDetailsService;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    private final MeterRegistry meterRegistry;

    private final MerchantConfigGrpcMapper mapper;

    public MerchantDetailsControllerGrpc(MerchantDetailsService merchantDetailsService,
                                         MerchantConfigService merchantConfigService, VariableService variableService,
                                         MeterRegistry meterRegistry, MerchantConfigGrpcMapper mapper) {
        this.merchantDetailsService = merchantDetailsService;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.meterRegistry = meterRegistry;
        this.mapper = mapper;
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
        Pageable pageable = mapper.createPageable(request.getPagination());
        Page<MerchantConfigDTO> page = merchantConfigService.findAll(pageable, mapToMerchantConfigRequest(request));

        List<MerchantConfigDTOGrpc> grpcData = page.getContent().stream()
                .map(mapper::mapToGrpcDto)
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
                default:
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
        Merchant merchant = Merchant.valueOf(request.getMerchant());
        if (request.hasIsUp()) {
            merchantConfigService.changeOrder(merchant, request.getIsUp().getValue());
        } else if (request.hasNewOrder()) {
            merchantConfigService.changeOrder(merchant, request.getNewOrder().getValue());
        }
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVariable(GetVariableRequestGrpc request, StreamObserver<VariableDTOGrpc> responseObserver) {
        VariableType type = VariableType.valueOf(request.getVariableType().name());
        var entity = variableService.findByTypeAndConfigType(type, ConfigType.valueOf(request.getConfigType().name()));

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
        variableService.update(type, ConfigType.valueOf(request.getConfigType().name()), newValue);

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
            log.error("Ошибка в gRPC методе sendReceipt: {}", e.getMessage());
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
