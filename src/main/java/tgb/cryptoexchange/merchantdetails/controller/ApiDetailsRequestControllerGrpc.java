package tgb.cryptoexchange.merchantdetails.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.service.ApiDetailsRequestProcessorService;
import tgb.cryptoexchange.merchantdetails.mapper.ApiDetailsRequestMapper;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@GrpcService(interceptors = {TestDetailsInterceptor.class})
public class ApiDetailsRequestControllerGrpc extends ApiDetailsRequestServiceGrpc.ApiDetailsRequestServiceImplBase {

    private final ApiDetailsRequestProcessorService processorService;

    private final ApiDetailsRequestMapper mapper;

    private final ThreadPoolTaskExecutor detailsRequestSearchExecutorApi;

    private final MerchantDetailsService merchantDetailsService;

    public ApiDetailsRequestControllerGrpc(ApiDetailsRequestProcessorService processorService,
                                           ThreadPoolTaskExecutor detailsRequestSearchExecutorApi,
                                           ApiDetailsRequestMapper mapper, MerchantDetailsService merchantDetailsService) {
        this.detailsRequestSearchExecutorApi = detailsRequestSearchExecutorApi;
        this.processorService = processorService;
        this.mapper = mapper;
        this.merchantDetailsService = merchantDetailsService;
    }

    @Override
    public void merchantCallbackRequest(MerchantCallbackGrpc requestGrpc, StreamObserver<Empty> responseObserver) {
        try (var ignored = MDC.putCloseable("logDest", "api")) {
            Merchant merchant;
            try {
                if (requestGrpc.getMerchant().isBlank()) {
                    throw new IllegalArgumentException("Merchant name is empty");
                }
                merchant = Merchant.valueOf(requestGrpc.getMerchant());
            } catch (IllegalArgumentException e) {
                log.error("Некорректный мерчант в callback запросе: '{}'", requestGrpc.getMerchant(), e);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Invalid merchant: " + requestGrpc.getMerchant())
                        .asRuntimeException());
                return;
            }

            try {
                merchantDetailsService.updateStatus(
                        Merchant.valueOf(requestGrpc.getMerchant()),
                        requestGrpc.getMerchantOrderId(),
                        requestGrpc.getStatus(),
                        requestGrpc.getStatusDescription()
                );
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            } catch (ServiceUnavailableException e) {
                log.error("Сервис недоступен при обновлении статуса мерчанта {}: {}", merchant, e.getMessage(), e);
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription(e.getMessage())
                        .asRuntimeException());
            } catch (Exception e) {
                log.error("Ошибка при обновлении статуса для мерчанта {}: {}", merchant, e.getMessage(), e);
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal error: " + e.getMessage())
                        .asRuntimeException());
            }
        }
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
                .setRequestId(requestGrpc.getRequestId())
                .setMerchant(Merchant.LOTRIEN.name())
                .setOrderId(UUID.randomUUID().toString())
                .setOrderStatus(tgb.cryptoexchange.merchantdetails.details.lotrien.Status.CREATED.name())
                .setDetails(DetailsGrpc.newBuilder()
                        .setBank("T-BANK")
                        .setRequestMethod(requestGrpc.getRequestMethod(0))
                        .setDetails("1111 2222 3n3n3n3n 4444")
                        .build())
                .setAmount(requestGrpc.getAmount())
                .build();
    }

}
