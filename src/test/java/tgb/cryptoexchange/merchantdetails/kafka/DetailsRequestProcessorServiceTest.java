package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailsRequestProcessorServiceTest {

    @Mock
    private MerchantDetailsService merchantDetailsService;

    @Mock
    private KafkaTemplate<String, DetailsResponse> kafkaTemplate;

    private DetailsRequestProcessorService service;

    @BeforeEach
    void setUp() {
        service = new DetailsRequestProcessorService(merchantDetailsService, kafkaTemplate,
                "merchant-details-request-topic");
    }

    @Test
    void receiveShouldSendEmptyResponseIfDetailsIsEmpty() {
        DetailsRequest request = new DetailsRequest();
        String id = UUID.randomUUID().toString();
        request.setRequestId(id);
        when(merchantDetailsService.getDetails(request, Arrays.asList(Merchant.values()))).thenReturn(Optional.empty());
        service.process(request, Arrays.asList(Merchant.values()));
        ArgumentCaptor<DetailsResponse> captor = ArgumentCaptor.forClass(DetailsResponse.class);
        verify(kafkaTemplate).send(eq("merchant-details-request-topic"), eq(id), captor.capture());
        assertEquals(id, captor.getValue().getRequestId());
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
        DetailsResponse response = new DetailsResponse();
        response.setDetails("ALFA 1234 1234 1234 1234");
        response.setMerchant(Merchant.ALFA_TEAM);
        response.setMerchantOrderStatus("PENDING");
        response.setAmount(5000);
        when(merchantDetailsService.getDetails(request, Arrays.asList(Merchant.values()))).thenReturn(Optional.of(response));
        service.process(request, Arrays.asList(Merchant.values()));
        ArgumentCaptor<DetailsResponse> captor = ArgumentCaptor.forClass(DetailsResponse.class);
        verify(kafkaTemplate).send(eq("merchant-details-request-topic"), eq(id), captor.capture());
        DetailsResponse actual = captor.getValue();
        assertAll(
                () -> assertEquals(id, actual.getRequestId()),
                () -> assertEquals(details, actual.getDetails()),
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals(status, actual.getMerchantOrderStatus()),
                () -> assertEquals(amount, actual.getAmount())
        );
    }
}