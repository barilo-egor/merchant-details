package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Optional;

@Service
public class DetailsRequestProcessorService {

    private final MerchantDetailsService merchantDetailsService;

    private final KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate;

    private final String detailsResponseFoundTopic;

    public DetailsRequestProcessorService(MerchantDetailsService merchantDetailsService,
                                          KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate,
                                          @Value("${kafka.topic.merchant-details.response}") String detailsResponseFoundTopic) {
        this.merchantDetailsService = merchantDetailsService;
        this.detailsResponseKafkaTemplate = detailsResponseKafkaTemplate;
        this.detailsResponseFoundTopic = detailsResponseFoundTopic;
    }

    @Async("detailsRequestSearchExecutor")
    public void process(DetailsRequest detailsRequest) {
        Optional<DetailsResponse> detailsResponse = merchantDetailsService.getDetails(detailsRequest);
        DetailsResponse result;
        result = detailsResponse.orElseGet(DetailsResponse::new);
        result.setRequestId(detailsRequest.getRequestId());
        detailsResponseKafkaTemplate.send(detailsResponseFoundTopic, result.getRequestId(), result);
    }
}
