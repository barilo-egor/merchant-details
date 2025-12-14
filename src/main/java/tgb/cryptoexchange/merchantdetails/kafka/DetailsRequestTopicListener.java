package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;

@Service
@Profile("!kafka-disabled")
public class DetailsRequestTopicListener {

    private final DetailsRequestProcessorService detailsRequestProcessorService;

    public DetailsRequestTopicListener(DetailsRequestProcessorService detailsRequestProcessorService) {
        this.detailsRequestProcessorService = detailsRequestProcessorService;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.request}", groupId = "${kafka.group-id}")
    public void receive(DetailsRequest request) {
        detailsRequestProcessorService.process(request);
    }
}
