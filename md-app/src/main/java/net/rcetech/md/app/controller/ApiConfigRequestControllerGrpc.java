package net.rcetech.md.app.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import net.rcetech.md.app.grpc.generated.*;
import net.rcetech.md.meta.dto.ApiMerchantConfigDTO;
import net.rcetech.md.app.mapper.ApiMerchantConfigGrpcMapper;
import net.rcetech.md.app.service.ApiMerchantConfigService;

import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
public class ApiConfigRequestControllerGrpc extends ApiMerchantConfigServiceGrpc.ApiMerchantConfigServiceImplBase {

    private final ApiMerchantConfigService apiMerchantConfigService;

    private final ApiMerchantConfigGrpcMapper mapper;

    public ApiConfigRequestControllerGrpc(ApiMerchantConfigService apiMerchantConfigService,
                                          ApiMerchantConfigGrpcMapper mapper) {
        this.apiMerchantConfigService = apiMerchantConfigService;
        this.mapper = mapper;
    }

    @Override
    public void findAll(FindAllApiMerchantConfigsRequestGrpc request,
                        StreamObserver<FindAllApiMerchantConfigsResponseGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {
            UUID ownerId;
            try {
                ownerId = parseOwnerId(request.getOwnerId());
            } catch (IllegalArgumentException e) {
                log.error("Некорректный ownerId в запросе получения конфигов мерчантов: '{}'", request.getOwnerId(), e);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Invalid ownerId: " + request.getOwnerId())
                        .asRuntimeException());
                return;
            }

            List<ApiMerchantConfigItemGrpc> configList = apiMerchantConfigService.findAllAndCreateIfNotExist(ownerId).stream()
                    .map(mapper::mapToGrpcApiMerchantConfigItem)
                    .toList();

            FindAllApiMerchantConfigsResponseGrpc response = FindAllApiMerchantConfigsResponseGrpc.newBuilder()
                    .addAllConfigs(configList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private UUID parseOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner id is empty");
        }
        return UUID.fromString(ownerId);
    }

    @Override
    public void update(UpdateApiMerchantConfigItemGrpc request,
                       StreamObserver<ApiMerchantConfigItemGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {
            ApiMerchantConfigDTO updated = apiMerchantConfigService.update(mapper.mapToUpdateMerchantConfigDTO(request));

            responseObserver.onNext(mapper.mapToGrpcApiMerchantConfigItem(updated));
            responseObserver.onCompleted();
        }
    }

}
