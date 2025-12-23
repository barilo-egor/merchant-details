package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

@Service
@Slf4j
public class DetailsRequestProcessorService {

    private final MerchantDetailsService merchantDetailsService;

    private final KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate;

    private final String detailsResponseFoundTopic;

    private final ThreadPoolTaskExecutor detailsRequestSearchExecutor;

    private final Map<Long, Future<Void>> activeSearchMap;

    public DetailsRequestProcessorService(MerchantDetailsService merchantDetailsService,
                                          KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate,
                                          @Value("${kafka.topic.merchant-details.response}") String detailsResponseFoundTopic,
                                          ThreadPoolTaskExecutor detailsRequestSearchExecutor,
                                          Map<Long, Future<Void>> activeSearchMap) {
        this.merchantDetailsService = merchantDetailsService;
        this.detailsResponseKafkaTemplate = detailsResponseKafkaTemplate;
        this.detailsResponseFoundTopic = detailsResponseFoundTopic;
        this.detailsRequestSearchExecutor = detailsRequestSearchExecutor;
        this.activeSearchMap = activeSearchMap;
    }

    public void process(DetailsRequest detailsRequest, Collection<Merchant> merchants) {
        Future<Void> future = detailsRequestSearchExecutor.submit(() -> {
            DetailsResponse result;
            try {
                Optional<DetailsResponse> detailsResponse = merchantDetailsService.getDetails(detailsRequest, merchants);
                if (!Thread.currentThread().isInterrupted()) {
                    result = detailsResponse.orElseGet(DetailsResponse::new);
                    result.setRequestId(detailsRequest.getRequestId());
                    detailsResponseKafkaTemplate.send(detailsResponseFoundTopic, result.getRequestId(), result);
                }
            } catch (Exception e) {
                result = new DetailsResponse();
                result.setRequestId(detailsRequest.getRequestId());
                detailsResponseKafkaTemplate.send(detailsResponseFoundTopic, result.getRequestId(), result);
            } finally {
                activeSearchMap.remove(detailsRequest.getId());
            }
            return null;
        });
        activeSearchMap.put(detailsRequest.getId(), future);
    }

    public void stop(Long id) {
        if (activeSearchMap.containsKey(id)) {
            if (activeSearchMap.get(id).cancel(true)) {
                log.debug("Поиск реквизитов для сделки {} остановлен.", id);
            } else {
                log.debug("Поиск реквизитов для сделки {} не может быть остановлен. Скорее всего поиск уже завершился.", id);
            }
        } else {
            log.debug("Поиск реквизитов для сделки {} не найден.", id);
        }
    }
}
