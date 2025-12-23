package tgb.cryptoexchange.merchantdetails.kafka;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.controller.MerchantDetailsController;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.service.MerchantApiService;

@Service
@Profile("!kafka-disabled")
public class DetailsRequestTopicListener {

    private final DetailsRequestProcessorService detailsRequestProcessorService;

    private final MerchantApiService merchantApiService;

    public DetailsRequestTopicListener(DetailsRequestProcessorService detailsRequestProcessorService, MerchantApiService merchantApiService) {
        this.detailsRequestProcessorService = detailsRequestProcessorService;
        this.merchantApiService = merchantApiService;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.request}", groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload DetailsRequest request,
                        @Header(name = "API-version", defaultValue = MerchantDetailsController.VERSION_0_9_1) String version) {
        detailsRequestProcessorService.process(request, merchantApiService.getMerchantsByApiVersion(version));
    }
}
