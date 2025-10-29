package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Сервис для выполнения запросов на создание ордера мерчантам.
 * @param <T> тип ответа от мерчанта
 */
@Slf4j
public abstract class MerchantOrderCreationService<T> {

    private final WebClient webClient;

    private final Class<T> responseType;

    protected MerchantOrderCreationService(WebClient webClient, Class<T> responseType) {
        this.webClient = webClient;
        this.responseType = responseType;
    }

    public Optional<RequisiteResponse> createOrder(RequisiteRequest requisiteRequest) {
        T response;
        try {
            response = webClient.method(method())
                    .uri(uriBuilder())
                    .headers(headers(requisiteRequest))
                    .bodyValue(body(requisiteRequest))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientException | JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (requisiteRequest={}): {}",
                    currentTime, getMerchant().name(), requisiteRequest.toString(), e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while creating order: " + currentTime + ".", e );
        }
        return buildResponse(response);
    }

    protected HttpMethod method() {
        return HttpMethod.POST;
    }

    public abstract Merchant getMerchant();

    protected abstract Function<UriBuilder, URI> uriBuilder();

    protected abstract Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest);

    protected abstract String body(RequisiteRequest requisiteRequest) throws JsonProcessingException;

    protected abstract Optional<RequisiteResponse> buildResponse(T response);
}
