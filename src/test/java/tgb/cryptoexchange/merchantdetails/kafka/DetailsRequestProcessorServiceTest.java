package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailsRequestProcessorServiceTest {

    @Mock
    private MerchantDetailsService merchantDetailsService;

    @Mock
    private KafkaTemplate<String, DetailsResponse> kafkaTemplate;

    @Mock
    private ThreadPoolTaskExecutor detailsRequestSearchExecutor;

    @Mock
    private Map<Long, Future<Void>> activeSearchMap;

    @Mock
    private Future<Void> futureMock;

    private DetailsRequestProcessorService service;

    @BeforeEach
    void setUp() {
        service = new DetailsRequestProcessorService(merchantDetailsService, kafkaTemplate,
                "merchant-details-request-topic", detailsRequestSearchExecutor,
                activeSearchMap);
    }

    @Test
    void receiveShouldSendEmptyResponseIfDetailsIsEmpty() {
        DetailsRequest request = new DetailsRequest();
        String id = UUID.randomUUID().toString();
        request.setRequestId(id);
        request.setId(12352963876L);
        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            Object result = callable.call();
            return CompletableFuture.completedFuture(result);
        }).when(detailsRequestSearchExecutor).submit((Callable<?>) any());
        when(merchantDetailsService.getDetails(request, Arrays.asList(Merchant.values()))).thenReturn(Optional.empty());
        service.process(request, Arrays.asList(Merchant.values()));
        ArgumentCaptor<DetailsResponse> captor = ArgumentCaptor.forClass(DetailsResponse.class);
        verify(kafkaTemplate).send(eq("merchant-details-request-topic"), eq(id), captor.capture());
        verify(activeSearchMap).put(eq(12352963876L), any());
        verify(activeSearchMap).remove(12352963876L);
        assertEquals(id, captor.getValue().getRequestId());
        assertNull(captor.getValue().getDetails());
    }

    @Test
    void receiveShouldSendEmptyResponseIfExceptionWasThrown() {
        DetailsRequest request = new DetailsRequest();
        String id = UUID.randomUUID().toString();
        request.setRequestId(id);
        request.setId(12352963876L);
        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            Object result = callable.call();
            return CompletableFuture.completedFuture(result);
        }).when(detailsRequestSearchExecutor).submit((Callable<?>) any());
        when(merchantDetailsService.getDetails(request, Arrays.asList(Merchant.values()))).thenThrow(RuntimeException.class);
        service.process(request, Arrays.asList(Merchant.values()));
        ArgumentCaptor<DetailsResponse> captor = ArgumentCaptor.forClass(DetailsResponse.class);
        verify(kafkaTemplate).send(eq("merchant-details-request-topic"), eq(id), captor.capture());
        verify(activeSearchMap).put(eq(12352963876L), any());
        verify(activeSearchMap).remove(12352963876L);
        assertEquals(id, captor.getValue().getRequestId());
        assertNull(captor.getValue().getDetails());
    }

    @CsvSource("""
            ALFA 1234 1234 1234 1234,ALFA_TEAM,PENDING,5000
            Ozon bank 79879878787,ONLY_PAYS,PROCESS,6432
            """)
    @ParameterizedTest
    void receiveShouldSendDetailsObjectIfDetailsFound(String details, Merchant merchant, String status, Integer amount) {
        DetailsRequest request = new DetailsRequest();
        String id = UUID.randomUUID().toString();
        request.setRequestId(id);
        request.setId(12352963876L);
        DetailsResponse response = new DetailsResponse();
        response.setDetails("ALFA 1234 1234 1234 1234");
        response.setMerchant(Merchant.ALFA_TEAM);
        response.setMerchantOrderStatus("PENDING");
        response.setAmount(5000);
        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            Object result = callable.call();
            return CompletableFuture.completedFuture(result);
        }).when(detailsRequestSearchExecutor).submit((Callable<?>) any());
        when(merchantDetailsService.getDetails(request, Arrays.asList(Merchant.values()))).thenReturn(Optional.of(response));
        service.process(request, Arrays.asList(Merchant.values()));
        ArgumentCaptor<DetailsResponse> captor = ArgumentCaptor.forClass(DetailsResponse.class);
        verify(kafkaTemplate).send(eq("merchant-details-request-topic"), eq(id), captor.capture());
        verify(activeSearchMap).put(eq(12352963876L), any());
        verify(activeSearchMap).remove(12352963876L);
        DetailsResponse actual = captor.getValue();
        assertAll(
                () -> assertEquals(id, actual.getRequestId()),
                () -> assertEquals(details, actual.getDetails()),
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals(status, actual.getMerchantOrderStatus()),
                () -> assertEquals(amount, actual.getAmount())
        );
    }

    @ValueSource(longs = {1241653L,1L,125163L})
    @ParameterizedTest
    void stopShouldCallCancelIfContainsId(Long id) {
        when(activeSearchMap.containsKey(id)).thenReturn(true);
        doReturn(futureMock).when(activeSearchMap).get(id);
        service.stop(id);
        verify(futureMock).cancel(true);
    }

    @ValueSource(longs = {1241653L,1L,125163L})
    @ParameterizedTest
    void stopShouldNotCallCancelIfNotContainsId(Long id) {
        when(activeSearchMap.containsKey(id)).thenReturn(false);
        service.stop(id);
        verify(activeSearchMap, times(0)).get(id);
    }
}