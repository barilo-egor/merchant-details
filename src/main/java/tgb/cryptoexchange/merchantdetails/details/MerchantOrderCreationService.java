package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
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

    protected MerchantOrderCreationService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<RequisiteResponse> createOrder(RequisiteRequest requisiteRequest) {
        WebClient. RequestHeadersSpec<?> requestHeadersSpec;
        try {
            requestHeadersSpec = webClient.method(method())
                    .uri(uriBuilder())
                    .headers(headers(requisiteRequest))
                    .bodyValue(body(requisiteRequest));
        } catch (Exception e) {
            log.error("Ошибка при попытке формирования запроса: {}", e.getMessage(), e);
            return Optional.empty();
        }
        T response;
        try {
            response = requestHeadersSpec
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<T>() {})
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса к мерчанту(method={},amount={}): {}",
                    requisiteRequest.getMethod(), requisiteRequest.getAmount(), e.getMessage(), e);
            return Optional.empty();
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
