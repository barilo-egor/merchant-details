package tgb.cryptoexchange.merchantdetails.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.grpc.generated.MerchantCallbackGrpc;
import tgb.cryptoexchange.merchantdetails.details.MerchantService;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.detailsapi.service.ApiDetailsRequestProcessorService;
import tgb.cryptoexchange.merchantdetails.mapper.ApiDetailsRequestMapper;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    private MerchantDetailsService merchantDetailsService;

    @Mock
    private StreamObserver<Empty> responseObserver;

    private ApiDetailsRequestControllerGrpc controller;

    @BeforeEach
    void setUp() {
        controller = new ApiDetailsRequestControllerGrpc(
                processorService,
                detailsRequestSearchExecutorApi,
                mapper,
                merchantDetailsService
        );
    }

    @Test
    void merchantCallbackRequest_Success() {
        MerchantCallbackGrpc request = MerchantCallbackGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setMerchantOrderId("order-123")
                .setStatus("PAID")
                .setStatusDescription("Payment successful")
                .build();

        controller.merchantCallbackRequest(request, responseObserver);

        verify(merchantDetailsService).updateStatus(Merchant.ALFA_TEAM, "order-123", "PAID", "Payment successful");
        verify(responseObserver).onNext(Empty.newBuilder().build());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void merchantCallbackRequest_InvalidMerchant() {
        MerchantCallbackGrpc request = MerchantCallbackGrpc.newBuilder()
                .setMerchant("UNKNOWN_MERCHANT")
                .setMerchantOrderId("order-123")
                .setStatus("PAID")
                .build();

        controller.merchantCallbackRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void merchantCallbackRequest_BlankMerchant() {
        MerchantCallbackGrpc request = MerchantCallbackGrpc.newBuilder()
                .setMerchant("")
                .setMerchantOrderId("order-123")
                .setStatus("PAID")
                .build();

        controller.merchantCallbackRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    void merchantCallbackRequest_ServiceThrowsServiceUnavailableException() {
        MerchantCallbackGrpc request = MerchantCallbackGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setMerchantOrderId("order-123")
                .setStatus("PAID")
                .setStatusDescription("desc")
                .build();
        doThrow(new ServiceUnavailableException("Kafka down")).when(merchantDetailsService)
                .updateStatus(Merchant.ALFA_TEAM, "order-123", "PAID", "desc");

        controller.merchantCallbackRequest(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.Code.UNAVAILABLE, ex.getStatus().getCode());
    }
}
