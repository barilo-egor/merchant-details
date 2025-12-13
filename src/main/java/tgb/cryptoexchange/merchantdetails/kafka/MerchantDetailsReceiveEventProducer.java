package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;

import java.time.Instant;
import java.util.UUID;

@Service
@Profile({"!kafka-disabled"})
public class MerchantDetailsReceiveEventProducer {

    private final KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate;

    private final String receiveEventTopicName;

    public MerchantDetailsReceiveEventProducer(KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate,
                                               @Value("${kafka.topic.merchant-details.receive}") String receiveEventTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.receiveEventTopicName = receiveEventTopicName;
    }

    public void put(Merchant merchant, DetailsRequest detailsRequest, DetailsResponse detailsResponse) {
        MerchantDetailsReceiveEvent event = new MerchantDetailsReceiveEvent();
        event.setDealId(detailsRequest.getId());
        event.setUserId(detailsRequest.getChatId());
        event.setInitiatorApp(detailsRequest.getInitiatorApp());
        event.setCreatedAt(Instant.now());
        event.setMerchant(merchant.name());
        event.setMerchantOrderId(detailsResponse.getMerchantOrderId());
        event.setRequestedAmount(detailsRequest.getAmount());
        event.setMerchantAmount(detailsResponse.getAmount());
        detailsRequest.getMethod(merchant).ifPresent(event::setMethod);
        event.setDetails(detailsResponse.getDetails());
        kafkaTemplate.send(receiveEventTopicName, UUID.randomUUID().toString(), event);
    }
}
