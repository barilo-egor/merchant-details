package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;

import java.time.Instant;
import java.util.Objects;
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

    public void put(Merchant merchant, OrderCreationRequest detailsRequest, DetailsResponse detailsResponse) {
        MerchantDetailsReceiveEvent event = new MerchantDetailsReceiveEvent();
        event.setOperationId(Objects.nonNull(detailsRequest.getId()) ? detailsRequest.getId().toString() : null);
        event.setActorId(Objects.nonNull(detailsRequest.getChatId()) ? detailsRequest.getChatId().toString() : null);
        event.setInitiatorApp(detailsRequest.getInitiatorApp());
        event.setCreatedAt(Instant.now());
        event.setMerchant(merchant.name());
        event.setMerchantOrderId(detailsResponse.getMerchantOrderId());
        event.setRequestedAmount(detailsRequest.getAmount());
        event.setMerchantAmount(detailsResponse.getAmount());
        event.setMethod(detailsRequest.getMethod());
        event.setDetails(detailsResponse.getDetails());
        event.setPaymentLink(detailsResponse.getQr());
        kafkaTemplate.send(receiveEventTopicName, UUID.randomUUID().toString(), event);
    }
}
