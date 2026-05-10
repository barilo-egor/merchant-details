package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.grpc.generated.ApiDetailsRequestServiceGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsRequestGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsResponseGrpc;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.service.ApiDetailsRequestProcessorService;
import tgb.cryptoexchange.merchantdetails.mapper.ApiDetailsRequestMapper;

import java.util.concurrent.RejectedExecutionException;

@Slf4j
@GrpcService
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
            try {
                detailsRequestSearchExecutorApi.execute(() -> {
                    try {
                        ApiDetailsRequest detailsRequest = mapper.mapGrpcToDto(requestGrpc);

                        ApiDetailsResponse response = processorService.process(detailsRequest);
                        responseObserver.onNext(mapper.mapToGrpcDto(response));
                        responseObserver.onCompleted();
                    } catch (StatusRuntimeException e) {
                        responseObserver.onError(e);
                    }
                });
            } catch (RejectedExecutionException e) {
                responseObserver.onError(Status.RESOURCE_EXHAUSTED
                        .withDescription("Server is overloaded")
                        .asRuntimeException());
            }
        }
    }

}
