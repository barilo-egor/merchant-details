package net.rcetech.md.app.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.app.grpc.generated.*;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.app.detailsapi.dto.ApiDetailsRequest;
import net.rcetech.md.meta.detailsapi.dto.ApiDetailsResponse;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import net.rcetech.md.app.detailsapi.service.ApiDetailsRequestProcessorService;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.app.mapper.ApiDetailsRequestMapper;
import net.rcetech.md.app.service.MerchantDetailsService;
import net.rcetech.md.integrations.service.RequestMethodService;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import static net.rcetech.md.integrations.platform.lotrien.Status.CREATED;

@Slf4j
@GrpcService(interceptors = {TestDetailsInterceptor.class})
public class ApiDetailsRequestControllerGrpc extends ApiDetailsRequestServiceGrpc.ApiDetailsRequestServiceImplBase {

    private static final String MDC_KEY_LOG_DEST = "logDest";
    private static final String MDC_VALUE_API = "api";

    private final ApiDetailsRequestProcessorService processorService;
    private final ApiDetailsRequestMapper mapper;
    private final ThreadPoolTaskExecutor detailsRequestSearchExecutorApi;
    private final MerchantDetailsService merchantDetailsService;
    private final RequestMethodService requestMethodService;

    public ApiDetailsRequestControllerGrpc(
            ApiDetailsRequestProcessorService processorService,
            @Qualifier("detailsRequestSearchExecutorApi") ThreadPoolTaskExecutor detailsRequestSearchExecutorApi,
            ApiDetailsRequestMapper mapper,
            MerchantDetailsService merchantDetailsService,
            RequestMethodService requestMethodService) {
        this.processorService = processorService;
        this.detailsRequestSearchExecutorApi = detailsRequestSearchExecutorApi;
        this.mapper = mapper;
        this.merchantDetailsService = merchantDetailsService;
        this.requestMethodService = requestMethodService;
    }

    @Override
    public void cancelOrderRequest(CancelOrderRequestGrpc requestGrpc, StreamObserver<Empty> responseObserver) {
        try (var ignored = MDC.putCloseable(MDC_KEY_LOG_DEST, MDC_VALUE_API)) {
            Merchant merchant;
            try {
                merchant = parseMerchant(requestGrpc.getMerchant());
            } catch (IllegalArgumentException e) {
                log.error("Некорректный мерчант в запросе отмены ордера: '{}'", requestGrpc.getMerchant(), e);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Invalid merchant: " + requestGrpc.getMerchant())
                        .asRuntimeException());
                return;
            }

            if (requestGrpc.getOrderId().isBlank()) {
                log.error("Пустой orderId в запросе отмены ордера для мерчанта {}", merchant);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Order id is empty")
                        .asRuntimeException());
                return;
            }

            if (requestGrpc.getMethod().isBlank()) {
                log.error("Пустой request метод в запросе отмены ордера для мерчанта {}", merchant);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Request method is empty")
                        .asRuntimeException());
                return;
            }

            try {
                CancelOrderRequest domainRequest = toCancelOrderRequest(merchant, requestGrpc);
                merchantDetailsService.cancelOrder(merchant, domainRequest);
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            } catch (MerchantMethodNotFoundException | IllegalArgumentException e) {
                log.error("Некорректный метод в запросе отмены ордера для мерчанта {}: {}", merchant, e.getMessage(), e);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription(e.getMessage())
                        .asRuntimeException());
            } catch (ServiceUnavailableException e) {
                log.error("Сервис недоступен при отмене ордера мерчанта {}: {}", merchant, e.getMessage(), e);
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription(e.getMessage())
                        .asRuntimeException());
            } catch (Exception e) {
                log.error("Ошибка при отмене ордера для мерчанта {}: {}", merchant, e.getMessage(), e);
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal error: " + e.getMessage())
                        .asRuntimeException());
            }
        }
    }

    @Override
    public void detailsRequest(DetailsRequestGrpc requestGrpc, StreamObserver<DetailsResponseGrpc> responseObserver) {
        try (var ignored = MDC.putCloseable(MDC_KEY_LOG_DEST, MDC_VALUE_API)) {
            String testEnv = TestDetailsInterceptor.TEST_DETAILS_CTX_KEY.get();
            if (Boolean.parseBoolean(testEnv)) {
                responseObserver.onNext(fakeDetails(requestGrpc));
                responseObserver.onCompleted();
                return;
            }

            try {
                detailsRequestSearchExecutorApi.execute(() -> processDetailsRequest(requestGrpc, responseObserver));
            } catch (RejectedExecutionException e) {
                log.error("Пул потоков переполнен при обработке запроса реквизитов (requestId='{}')",
                        requestGrpc.getInternalId(), e);
                responseObserver.onError(Status.RESOURCE_EXHAUSTED
                        .withDescription("Server is overloaded")
                        .asRuntimeException());
            }
        }
    }

    private void processDetailsRequest(DetailsRequestGrpc requestGrpc, StreamObserver<DetailsResponseGrpc> responseObserver) {
        try {
            ApiDetailsRequest detailsRequest = mapper.mapGrpcToDto(requestGrpc);
            ApiDetailsResponse response = processorService.process(detailsRequest);
            responseObserver.onNext(mapper.mapToGrpcDto(response));
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.warn("gRPC ошибка при обработке запроса реквизитов (requestId='{}'): {}",
                    requestGrpc.getInternalId(), e.getMessage());
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса реквизитов (requestId='{}')",
                    requestGrpc.getInternalId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private Merchant parseMerchant(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("Merchant name is empty");
        }
        return Merchant.valueOf(merchantName);
    }

    private CancelOrderRequest toCancelOrderRequest(Merchant merchant, CancelOrderRequestGrpc requestGrpc) {
        RequestMethod requestMethod;
        try {
            requestMethod = RequestMethod.valueOf(requestGrpc.getMethod());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request method: " + requestGrpc.getMethod(), e);
        }

        String methodName = requestMethodService.getMerchantMethod(merchant, requestMethod);

        CancelOrderRequest domainRequest = new CancelOrderRequest();
        domainRequest.setOrderId(requestGrpc.getOrderId());
        domainRequest.setMethod(methodName);
        return domainRequest;
    }

    private DetailsResponseGrpc fakeDetails(DetailsRequestGrpc requestGrpc) {
        String requestMethod = requestGrpc.getRequestMethodCount() > 0
                ? requestGrpc.getRequestMethod(0)
                : "";

        return DetailsResponseGrpc.newBuilder()
                .setMerchant(Merchant.LOTRIEN.name())
                .setOrderId(UUID.randomUUID().toString())
                .setOrderStatus(CREATED.name())
                .setDetails(DetailsGrpc.newBuilder()
                        .setBank("T-BANK")
                        .setRequestMethod(requestMethod)
                        .setDetails("1111 2222 3n3n3n3n 4444")
                        .build())
                .setAmount(requestGrpc.getAmount())
                .build();
    }
}
