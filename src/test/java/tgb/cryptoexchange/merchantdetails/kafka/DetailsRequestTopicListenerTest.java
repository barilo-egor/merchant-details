package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.service.MerchantApiService;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailsRequestTopicListenerTest {

    @Mock
    private DetailsRequestProcessorService detailsRequestProcessorService;

    @Mock
    private MerchantApiService merchantApiService;

    @InjectMocks
    private DetailsRequestTopicListener detailsRequestTopicListener;

    @Test
    void receiveShouldCallServiceMethodWithAllMerchants() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setRequestId(UUID.randomUUID().toString());
        when(merchantApiService.getMerchantsByApiVersion(anyString())).thenReturn(new ArrayList<>());
        detailsRequestTopicListener.receive(detailsRequest, "0.10.0");
        verify(merchantApiService).getMerchantsByApiVersion("0.10.0");
        verify(detailsRequestProcessorService).process(eq(detailsRequest), anyList());
    }

    @ValueSource(strings = {"0.8", "0.9", "0.9.1"})
    @ParameterizedTest
    void receiveShouldCallServiceMethodWithAllMerchantsWithoutPlata(String version) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setRequestId(UUID.randomUUID().toString());
        when(merchantApiService.getMerchantsByApiVersion(anyString())).thenReturn(new ArrayList<>());
        detailsRequestTopicListener.receive(detailsRequest, version);
        verify(merchantApiService).getMerchantsByApiVersion(version);
        verify(detailsRequestProcessorService).process(eq(detailsRequest), anyList());
    }
}