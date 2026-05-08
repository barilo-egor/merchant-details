package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DetailsRequestTopicListenerTest {

    @Mock
    private DetailsRequestProcessorService detailsRequestProcessorService;

    @InjectMocks
    private DetailsRequestTopicListener detailsRequestTopicListener;

    @Test
    void receiveShouldCallServiceMethodWithAllMerchants() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        detailsRequest.setRequestId(UUID.randomUUID().toString());
        detailsRequestTopicListener.receive(detailsRequest);
        verify(detailsRequestProcessorService).process(detailsRequest);
    }
}