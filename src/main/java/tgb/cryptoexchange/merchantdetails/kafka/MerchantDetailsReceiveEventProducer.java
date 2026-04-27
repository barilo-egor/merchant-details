package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequestBot;
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

    public void put(Merchant merchant, String merchantMethod, DetailsRequestBot detailsRequest, DetailsResponse detailsResponse) {
        MerchantDetailsReceiveEvent event = new MerchantDetailsReceiveEvent();
        event.setDealId(Long.valueOf(detailsRequest.getId()));
        event.setUserId(Long.valueOf(detailsRequest.getUserId()));
        event.setInitiatorApp(detailsRequest.getInitiatorApp());
        event.setCreatedAt(Instant.now());
        event.setMerchant(merchant.name());
        event.setMerchantOrderId(detailsResponse.getMerchantOrderId());
        event.setRequestedAmount(detailsRequest.getAmount());
        event.setMerchantAmount(detailsResponse.getAmount());
        event.setMethod(merchantMethod);
        event.setDetails(detailsResponse.getFullDetails());
        event.setPaymentLink(detailsResponse.getQr());
        kafkaTemplate.send(receiveEventTopicName, UUID.randomUUID().toString(), event);
    }
}
