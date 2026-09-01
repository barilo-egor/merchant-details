package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.mapper.MerchantConfigGrpcMapper;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;

import java.util.List;

@Slf4j
@GrpcService
public class ApiConfigRequestControllerGrpc extends MerchantConfigServiceGrpc.MerchantConfigServiceImplBase {

    private final MerchantConfigService merchantConfigService;

    private final MerchantConfigGrpcMapper mapper;

    public ApiConfigRequestControllerGrpc(MerchantConfigService merchantConfigService,
                                          MerchantConfigGrpcMapper mapper) {
        this.merchantConfigService = merchantConfigService;
        this.mapper = mapper;
    }

    @Override
    public void findAll(FindAllMerchantConfigsRequestGrpc request,
                        StreamObserver<FindAllMerchantConfigsResponseGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {

            List<MerchantConfigItemGrpc> configList = merchantConfigService.findAll(mapper.mapToMerchantConfigRequest(request))
                    .stream()
                    .map(mapper::mapToGrpcMerchantConfigItem)
                    .toList();

            FindAllMerchantConfigsResponseGrpc response = FindAllMerchantConfigsResponseGrpc.newBuilder()
                    .addAllConfigs(configList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void update(UpdateMerchantConfigItemGrpc request,
                       StreamObserver<MerchantConfigItemGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {
            MerchantConfigDTO updated = merchantConfigService.update(mapper.mapToUpdateMerchantConfigDTO(request));

            responseObserver.onNext(mapper.mapToGrpcMerchantConfigItem(updated));
            responseObserver.onCompleted();
        }
    }

}
