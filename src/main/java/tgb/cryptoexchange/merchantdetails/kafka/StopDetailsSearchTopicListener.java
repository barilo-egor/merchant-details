package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Profile("!kafka-disabled")
@Slf4j
public class StopDetailsSearchTopicListener {

    private final DetailsRequestProcessorService detailsRequestProcessorService;

    public StopDetailsSearchTopicListener(DetailsRequestProcessorService detailsRequestProcessorService) {
        this.detailsRequestProcessorService = detailsRequestProcessorService;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.stop-search}", groupId = "${kafka.group-id}",
            containerFactory = "stopSearchRequestContainerFactory")
    public void receive(@Payload StopSearchRequest request) {
        log.debug("Поступил запрос на остановку поиска реквизитов по сделке {}", request.getId());
        detailsRequestProcessorService.stop(request.getId());
    }
}
