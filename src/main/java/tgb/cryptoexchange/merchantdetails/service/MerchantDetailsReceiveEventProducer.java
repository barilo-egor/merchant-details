package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.dto.MerchantDetailsReceiveEvent;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.time.Instant;

@Service
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
        event.setMethod(detailsRequest.getMethod());
        event.setDetails(detailsResponse.getDetails());
        kafkaTemplate.send(receiveEventTopicName, event);
    }
}
