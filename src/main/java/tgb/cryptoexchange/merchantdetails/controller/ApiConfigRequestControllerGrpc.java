package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.mapper.ApiMerchantConfigGrpcMapper;
import tgb.cryptoexchange.merchantdetails.service.ApiMerchantConfigService;

import java.util.List;

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

            List<ApiMerchantConfigItemGrpc> configList = apiMerchantConfigService.findAllAndCreateIfNotExist(
                            mapper.mapToApiMerchantConfigRequest(request)
                    ).stream()
                    .map(mapper::mapToGrpcApiMerchantConfigItem)
                    .toList();

            FindAllApiMerchantConfigsResponseGrpc response = FindAllApiMerchantConfigsResponseGrpc.newBuilder()
                    .addAllConfigs(configList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
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
