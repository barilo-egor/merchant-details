package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DetailsRequestTopicListenerTest {

    @Mock
    private DetailsRequestProcessorService detailsRequestProcessorService;

    @InjectMocks
    private DetailsRequestTopicListener detailsRequestTopicListener;

    @Captor
    private ArgumentCaptor<Set<Merchant>> merchantsCaptor;

    @Test
    void receiveShouldCallServiceMethodWithAllMerchants() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setRequestId(UUID.randomUUID().toString());
        detailsRequestTopicListener.receive(detailsRequest, "0.10");
        verify(detailsRequestProcessorService).process(detailsRequest, Arrays.asList(Merchant.values()));
    }

    @ValueSource(strings = {"0.8", "0.9", "0.9.1"})
    @ParameterizedTest
    void receiveShouldCallServiceMethodWithAllMerchantsWithoutPlata(String version) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setRequestId(UUID.randomUUID().toString());
        detailsRequestTopicListener.receive(detailsRequest, version);
        verify(detailsRequestProcessorService).process(eq(detailsRequest), merchantsCaptor.capture());
        assertFalse(merchantsCaptor.getValue().contains(Merchant.PLATA_PAYMENT));
    }
}