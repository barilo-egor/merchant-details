package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"kafka.topic.merchant-details.receive=test-topic"})
class MerchantDetailsReceiveEventProducerTest {

    @Mock
    private KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate;

    @CsvSource("""
            merchant-details-receive-v1,17551592595,398395786,banan,ALFA_TEAM,b9519d18-7ecf-47fd-ae74-0eca84d8656e,2500,2502,SBP,ALFA 79879878787
            merchant-details-receive-v2,17551564636,8050468384,money,WELL_BIT,869b6ba4-fc34-4df5-910c-cf69a05027b9,25300,25301,CARD,Банк развития 1234123412341234
            """)
    @ParameterizedTest
    void putShouldSendEventToTopic(String topic, Long dealId, Long userId, String appId, Merchant merchant, String orderId,
                                   Integer requestedAmount, Integer merchantAmount, String method, String details) {
        var merchantDetailsReceiveEventProducer = new MerchantDetailsReceiveEventProducer(kafkaTemplate, topic);
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setId(dealId);
        detailsRequest.setMethod(method);
        detailsRequest.setAmount(requestedAmount);
        detailsRequest.setChatId(userId);
        detailsRequest.setInitiatorApp(appId);
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(merchant);
        detailsResponse.setDetails(details);
        detailsResponse.setMerchantOrderId(orderId);
        detailsResponse.setAmount(merchantAmount);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MerchantDetailsReceiveEvent> eventCaptor = ArgumentCaptor.forClass(MerchantDetailsReceiveEvent.class);
        merchantDetailsReceiveEventProducer.put(merchant, detailsRequest, detailsResponse);
        verify(kafkaTemplate).send(topicCaptor.capture(), eventCaptor.capture());
        MerchantDetailsReceiveEvent event = eventCaptor.getValue();
        assertAll(
                () -> assertEquals(topic, topicCaptor.getValue()),
                () -> assertEquals(dealId, event.getDealId()),
                () -> assertEquals(userId, event.getUserId()),
                () -> assertEquals(appId, event.getInitiatorApp()),
                () -> assertEquals(merchant.name(), event.getMerchant()),
                () -> assertEquals(orderId, event.getMerchantOrderId()),
                () -> assertEquals(requestedAmount, event.getRequestedAmount()),
                () -> assertEquals(merchantAmount, event.getMerchantAmount()),
                () -> assertEquals(method, event.getMethod()),
                () -> assertEquals(details, event.getDetails())

        );
    }

}