package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.util.EnumUtils;
import tgb.cryptoexchange.merchantdetails.util.StringDecodeUtils;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Сервис для выполнения запросов на создание ордера мерчантам.
 *
 * @param <T> тип ответа от мерчанта
 */
@Slf4j
public abstract class MerchantOrderCreationService<T extends MerchantDetailsResponse> implements MerchantService {

    private final WebClient webClient;

    private final Class<T> responseType;

    protected ObjectMapper objectMapper;

    private RequestService requestService;

    protected MerchantOrderCreationService(WebClient webClient, Class<T> responseType) {
        this.webClient = webClient;
        this.responseType = responseType;
    }

    @Autowired
    public void setRequestService(RequestService requestService) {
        this.requestService = requestService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<DetailsResponse> createOrder(DetailsRequest detailsRequest) {
        String body = mapBody(detailsRequest);
        Optional<String> maybeRawResponse = makeRequest(detailsRequest, body);
        if (maybeRawResponse.isEmpty()) {
            return Optional.empty();
        }
        String rawResponse = maybeRawResponse.get();
        if (hasResponseNoDetailsErrorPredicate().test(rawResponse)) {
            return Optional.empty();
        }
        T response = mapResponse(rawResponse);
        validateResponse(response);
        if (!response.hasDetails()) {
            return Optional.empty();
        }
        return buildResponse(response);
    }

    private String mapBody(DetailsRequest detailsRequest) {
        try {
            return objectMapper.writeValueAsString(body(detailsRequest));
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при маппинге тела запроса(detailsRequest = {}): {}", currentTime, detailsRequest, e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while mapping body: " + currentTime + ".", e);
        }
    }

    private Optional<String> makeRequest(DetailsRequest detailsRequest, String body) {
        try {
            return Optional.of(
                    requestService.request(
                            webClient, method(), uriBuilder(detailsRequest),
                            headers(detailsRequest, body), body
                    )
            );
        } catch (Exception e) {
            if (isNoDetailsExceptionPredicate().test(e)) {
                return Optional.empty();
            }
            long currentTime = System.currentTimeMillis();
            if (e instanceof WebClientResponseException webClientResponseException) {
                String errorBody = switch (getMerchant()) {
                    case FOX_PAYS, MOBIUS ->
                            StringDecodeUtils.decodeUnicode(webClientResponseException.getResponseBodyAsString());
                    default -> webClientResponseException.getResponseBodyAsString();
                };
                log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (detailsRequest={}, body={}): {}, responseBody = {}",
                        currentTime, getMerchant().name(), detailsRequest.toString(), body, e.getMessage(),
                        errorBody, e);
            } else {
                log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (detailsRequest={}, body={}): {}",
                        currentTime, getMerchant().name(), detailsRequest.toString(), body, e.getMessage(), e);
            }
            throw new ServiceUnavailableException("Error occurred while creating order: " + currentTime + ".", e);
        }
    }

    private T mapResponse(String rawResponse) {
        try {
            return objectMapper.readValue(rawResponse, responseType);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка маппинга ответа мерчанта {}, оригинальный ответ= {}, ошибка: {}",
                    currentTime, getMerchant().name(), rawResponse, e.getMessage(), e
            );
            throw new ServiceUnavailableException("Error occurred while mapping merchant response: " + currentTime + ".", e);
        }
    }

    private void validateResponse(T response) {
        ValidationResult validationResult = response.validate();
        if (!validationResult.isValid()) {
            long currentTime = System.currentTimeMillis();
            log.error("Ответ мерчанта {} невалиден: {}", getMerchant().name(), validationResult.errorsToString());
            throw new ServiceUnavailableException("Mapped response is invalid: " + currentTime);
        }
    }

    protected HttpMethod method() {
        return HttpMethod.POST;
    }

    protected abstract Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest);

    protected abstract Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body);

    protected abstract Object body(DetailsRequest detailsRequest);

    protected abstract Optional<DetailsResponse> buildResponse(T response);

    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> false;
    }

    protected Predicate<String> hasResponseNoDetailsErrorPredicate() {
        return s -> false;
    }

    protected <E extends Enum<E>> E parseMethod(String value, Class<E> methodType) {
        return EnumUtils.valueOf(methodType, value,
                () -> new MerchantMethodNotFoundException("Method \"" + value + "\" for merchant "
                        + getMerchant().name() + " not found."));
    }
}
