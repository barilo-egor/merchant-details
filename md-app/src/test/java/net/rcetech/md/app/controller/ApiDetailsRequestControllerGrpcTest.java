package net.rcetech.md.app.controller;

import com.google.protobuf.Empty;
import com.google.rpc.Code;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.app.grpc.generated.CancelOrderRequestGrpc;
import net.rcetech.md.app.grpc.generated.DetailsRequestGrpc;
import net.rcetech.md.app.grpc.generated.DetailsResponseGrpc;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.app.detailsapi.ProcessingContract;
import net.rcetech.md.app.detailsapi.dto.ApiDetailsRequest;
import net.rcetech.md.meta.detailsapi.dto.ApiDetailsResponse;
import net.rcetech.md.meta.detailsapi.dto.Details;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import net.rcetech.md.app.detailsapi.service.ApiDetailsRequestProcessorService;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.app.mapper.ApiDetailsRequestMapper;
import net.rcetech.md.app.mapper.GrpcValidator;
import net.rcetech.md.integrations.platform.PlatformService;
import net.rcetech.md.integrations.service.RequestMethodService;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiDetailsRequestControllerGrpcTest {

    @Mock
    private ApiDetailsRequestProcessorService processorService;

    @Mock
    private ThreadPoolTaskExecutor detailsRequestSearchExecutorApi;

    @Mock
    private ApiDetailsRequestMapper mapper;

    @Mock
    private PlatformService platformService;

    @Mock
    private RequestMethodService requestMethodService;

    @Mock
    private StreamObserver<Empty> responseObserver;

    @Mock
    private StreamObserver<DetailsResponseGrpc> detailsObserver;

    private ApiDetailsRequestControllerGrpc controller;

    @BeforeEach
    void setUp() {
        controller = new ApiDetailsRequestControllerGrpc(
                processorService,
                detailsRequestSearchExecutorApi,
                mapper,
                platformService,
                requestMethodService
        );
    }

    @Test
    void cancelOrderRequest_Success() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("order-123")
                .setMethod("SBP")
                .build();
        when(requestMethodService.getMerchantMethod(Merchant.ALFA_TEAM, RequestMethod.SBP))
                .thenReturn("SBP_MERCHANT");

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<CancelOrderRequest> requestCaptor = ArgumentCaptor.forClass(CancelOrderRequest.class);
        verify(platformService).cancelOrder(eq(Merchant.ALFA_TEAM), requestCaptor.capture());
        assertEquals("order-123", requestCaptor.getValue().getOrderId());
        assertEquals("SBP_MERCHANT", requestCaptor.getValue().getMethod());
        verify(responseObserver).onNext(Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void cancelOrderRequest_InvalidMerchant() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant("UNKNOWN_MERCHANT")
                .setOrderId("order-123")
                .setMethod("SBP")
                .build();

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void cancelOrderRequest_BlankOrderId() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("")
                .setMethod("SBP")
                .build();

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(platformService, never()).cancelOrder(any(), any());
    }

    @Test
    void cancelOrderRequest_BlankMethod() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("order-123")
                .setMethod("")
                .build();

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(platformService, never()).cancelOrder(any(), any());
    }

    @Test
    void cancelOrderRequest_ServiceThrowsServiceUnavailableException() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("order-123")
                .setMethod("SBP")
                .build();
        when(requestMethodService.getMerchantMethod(Merchant.ALFA_TEAM, RequestMethod.SBP))
                .thenReturn("SBP");
        doThrow(new ServiceUnavailableException("Service down")).when(platformService)
                .cancelOrder(eq(Merchant.ALFA_TEAM), any(CancelOrderRequest.class));

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.UNAVAILABLE, ex.getStatus().getCode());
    }

    @Test
    void cancelOrderRequest_InvalidRequestMethod() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("order-123")
                .setMethod("UNKNOWN_PAYMENT_METHOD")
                .build();

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(platformService, never()).cancelOrder(any(), any());
    }

    @Test
    void cancelOrderRequest_MethodNotFound() {
        CancelOrderRequestGrpc request = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setOrderId("order-123")
                .setMethod("CARD")
                .build();
        when(requestMethodService.getMerchantMethod(Merchant.ALFA_TEAM, RequestMethod.CARD))
                .thenThrow(new MerchantMethodNotFoundException("Method not found for merchant ALFA_TEAM and category CARD"));

        controller.cancelOrderRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(platformService, never()).cancelOrder(any(), any());
    }

    private ApiDetailsRequestControllerGrpc controllerWithRealMapper() {
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(detailsRequestSearchExecutorApi).execute(any(Runnable.class));
        return new ApiDetailsRequestControllerGrpc(processorService, detailsRequestSearchExecutorApi,
                new ApiDetailsRequestMapper(), platformService, requestMethodService);
    }

    private ApiDetailsResponse detailsResponse(String bank) {
        ApiDetailsResponse response = new ApiDetailsResponse();
        response.setMerchant(Merchant.ONLY_PAYS.name());
        response.setOrderId("m-order-1");
        response.setOrderStatus("PENDING");
        response.setDetails(Details.builder()
                .requestMethod(RequestMethod.SBP)
                .details("+79990000000")
                .bank(bank)
                .build());
        return response;
    }

    private int errorCode(StreamObserver<DetailsResponseGrpc> observer) {
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(observer).onError(errorCaptor.capture());
        com.google.rpc.Status status = StatusProto.fromThrowable(errorCaptor.getValue());
        return status != null ? status.getCode() : Status.fromThrowable(errorCaptor.getValue()).getCode().value();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "demo-user-1")
    void detailsRequest_ResponseSatisfiesProcessingContract(String userId) {
        DetailsRequestGrpc request = ProcessingContract.processingRequest(userId, "CARD", "SBP");
        when(processorService.process(any())).thenReturn(detailsResponse("Сбербанк"));

        controllerWithRealMapper().detailsRequest(request, detailsObserver);

        ArgumentCaptor<DetailsResponseGrpc> captor = ArgumentCaptor.forClass(DetailsResponseGrpc.class);
        verify(detailsObserver).onNext(captor.capture());
        verify(detailsObserver).onCompleted();
        ProcessingContract.assertSatisfies(captor.getValue(), request.getRequestMethodList());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "demo-user-1")
    void detailsRequest_PassesUserIdAsProcessingSentIt(String userId) {
        DetailsRequestGrpc request = ProcessingContract.processingRequest(userId, "CARD");
        when(processorService.process(any())).thenReturn(detailsResponse("Сбербанк"));

        controllerWithRealMapper().detailsRequest(request, detailsObserver);

        ArgumentCaptor<ApiDetailsRequest> captor = ArgumentCaptor.forClass(ApiDetailsRequest.class);
        verify(processorService).process(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(List.of(RequestMethod.CARD), captor.getValue().getRequestMethods());
        verify(detailsObserver, never()).onError(any());
    }

    @Test
    void detailsRequest_DetailsNotFound() {
        DetailsRequestGrpc request = ProcessingContract.processingRequest(null, "CARD");
        when(processorService.process(any()))
                .thenAnswer(invocation -> {
                    throw GrpcValidator.detailsNotFound(invocation.getArgument(0));
                });

        controllerWithRealMapper().detailsRequest(request, detailsObserver);

        assertEquals(Code.NOT_FOUND_VALUE, errorCode(detailsObserver));
        verify(detailsObserver, never()).onNext(any());
    }

    @Test
    void detailsRequest_ExecutorRejected() {
        doThrow(new RejectedExecutionException()).when(detailsRequestSearchExecutorApi).execute(any(Runnable.class));

        controller.detailsRequest(ProcessingContract.processingRequest(null, "CARD"), detailsObserver);

        assertEquals(Status.Code.RESOURCE_EXHAUSTED.value(), errorCode(detailsObserver));
        verifyNoInteractions(processorService);
    }

    @Test
    void detailsRequest_FakeDetailsSatisfyProcessingContract() {
        DetailsRequestGrpc request = ProcessingContract.processingRequest(null, "SBP");

        Context.current().withValue(TestDetailsInterceptor.TEST_DETAILS_CTX_KEY, "true")
                .run(() -> controller.detailsRequest(request, detailsObserver));

        ArgumentCaptor<DetailsResponseGrpc> captor = ArgumentCaptor.forClass(DetailsResponseGrpc.class);
        verify(detailsObserver).onNext(captor.capture());
        assertNotNull(captor.getValue());
        ProcessingContract.assertSatisfies(captor.getValue(), request.getRequestMethodList());
        verifyNoInteractions(processorService);
    }
}
