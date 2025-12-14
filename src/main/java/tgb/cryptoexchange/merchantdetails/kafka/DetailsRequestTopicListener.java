package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Optional;

@Service
@Profile("!kafka-disabled")
public class DetailsRequestTopicListener {

    private final MerchantDetailsService merchantDetailsService;

    private final KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate;

    private final String detailsResponseFoundTopic;

    public DetailsRequestTopicListener(MerchantDetailsService merchantDetailsService, KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate,
                                       @Value("${kafka.topic.merchant-details.response}") String detailsResponseFoundTopic) {
        this.merchantDetailsService = merchantDetailsService;
        this.detailsResponseKafkaTemplate = detailsResponseKafkaTemplate;
        this.detailsResponseFoundTopic = detailsResponseFoundTopic;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.request}")
    public void receive(DetailsRequest request) {
        Optional<DetailsResponse> detailsResponse = merchantDetailsService.getDetails(request);
        DetailsResponse result;
        result = detailsResponse.orElseGet(DetailsResponse::new);
        result.setRequestId(request.getRequestId());
        detailsResponseKafkaTemplate.send(detailsResponseFoundTopic, result.getRequestId(), result);
    }
}
