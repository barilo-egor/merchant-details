package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.grpc.generated.ApiDetailsRequestServiceGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsRequestGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsResponseGrpc;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.service.ApiDetailsRequestProcessorService;
import tgb.cryptoexchange.merchantdetails.mapper.ApiDetailsRequestMapper;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@GrpcService(interceptors = {TestDetailsInterceptor.class})
public class ApiDetailsRequestControllerGrpc extends ApiDetailsRequestServiceGrpc.ApiDetailsRequestServiceImplBase {

    private final ApiDetailsRequestProcessorService processorService;

    private final ApiDetailsRequestMapper mapper;

    private final ThreadPoolTaskExecutor detailsRequestSearchExecutorApi;

    public ApiDetailsRequestControllerGrpc(ApiDetailsRequestProcessorService processorService,
                                           ThreadPoolTaskExecutor detailsRequestSearchExecutorApi,
                                           ApiDetailsRequestMapper mapper) {
        this.detailsRequestSearchExecutorApi = detailsRequestSearchExecutorApi;
        this.processorService = processorService;
        this.mapper = mapper;
    }

    @Override
    public void detailsRequest(DetailsRequestGrpc requestGrpc, StreamObserver<DetailsResponseGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {

            String testEnv = TestDetailsInterceptor.TEST_DETAILS_CTX_KEY.get();
            if ("true".equalsIgnoreCase(testEnv)) {
                responseObserver.onNext(fakeDetails(requestGrpc));
                responseObserver.onCompleted();
                return;
            }

            try {
                detailsRequestSearchExecutorApi.execute(() -> {
                    try {
                        ApiDetailsRequest detailsRequest = mapper.mapGrpcToDto(requestGrpc);

                        ApiDetailsResponse response = processorService.process(detailsRequest);
                        responseObserver.onNext(mapper.mapToGrpcDto(response));
                        responseObserver.onCompleted();
                    } catch (StatusRuntimeException e) {
                        responseObserver.onError(e);
                    } catch (Exception e) {
                        responseObserver.onError(Status.INTERNAL
                                .withDescription("Internal error: " + e.getMessage())
                                .asRuntimeException());
                    }
                });
            } catch (RejectedExecutionException e) {
                responseObserver.onError(Status.RESOURCE_EXHAUSTED
                        .withDescription("Server is overloaded")
                        .asRuntimeException());
            }
        }
    }

    private DetailsResponseGrpc fakeDetails(DetailsRequestGrpc requestGrpc) {
        return DetailsResponseGrpc.newBuilder()
                .setRequestId(requestGrpc.getRequestId().getValue())
                .setMerchant(Merchant.LOTRIEN.name())
                .setOrderId(UUID.randomUUID().toString())
                .setOrderStatus(tgb.cryptoexchange.merchantdetails.details.lotrien.Status.CREATED.name())
                .setDetails(DetailsGrpc.newBuilder()
                        .setBank("T-BANK")
                        .setRequestMethod(requestGrpc.getRequestMethod(0))
                        .setDetails("1111 2222 3n3n3n3n 4444")
                        .build())
                .setAmount(requestGrpc.getAmount().getValue())
                .build();
    }

}
