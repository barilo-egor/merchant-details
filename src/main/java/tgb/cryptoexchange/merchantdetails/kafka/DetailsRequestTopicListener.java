package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;

@Service
@Profile("!kafka-disabled")
public class DetailsRequestTopicListener {

    private final DetailsRequestProcessorService detailsRequestProcessorService;

    public DetailsRequestTopicListener(DetailsRequestProcessorService detailsRequestProcessorService) {
        this.detailsRequestProcessorService = detailsRequestProcessorService;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.request}", groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload DetailsRequest request) {
        detailsRequestProcessorService.process(request);
    }
}
