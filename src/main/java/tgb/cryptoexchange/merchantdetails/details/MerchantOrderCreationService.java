package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.util.EnumUtils;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Validator validator;

    protected MerchantOrderCreationService(WebClient webClient, Class<T> responseType) {
        this.webClient = webClient;
        this.responseType = responseType;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    public Optional<DetailsResponse> createOrder(DetailsRequest detailsRequest) {
        T response;
        String rawResponse;
        try {
            String body = objectMapper.writeValueAsString(body(detailsRequest));
            rawResponse = webClient.method(method())
                    .uri(uriBuilder(detailsRequest))
                    .headers(headers(detailsRequest, body))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientException | JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (detailsRequest={}): {}",
                    currentTime, getMerchant().name(), detailsRequest.toString(), e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while creating order: " + currentTime + ".", e );
        }
        try {
            response = objectMapper.readValue(rawResponse, responseType);
            validateResponse(response, rawResponse);
            return buildResponse(response);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка маппинга ответа мерчанта {}, оригинальный ответ= {}, ошибка: {}",
                    currentTime, getMerchant().name(), rawResponse, e.getMessage(), e
            );
            throw new ServiceUnavailableException("Error occurred while mapping merchant response: " + currentTime + ".", e );
        }
    }

    private void validateResponse(T response, String rawResponse) {
        if (response == null) {
            throw new ServiceUnavailableException("Empty response from merchant " + getMerchant().name());
        }

        Set<ConstraintViolation<T>> violations = validator.validate(response);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown validation error");

            log.error("Invalid response from {}: {}. rawResponse={}", getMerchant().name(), errors, rawResponse);
            throw new ServiceUnavailableException("Invalid response from " + getMerchant().name() + ": " + errors);
        }
    }

    protected HttpMethod method() {
        return HttpMethod.POST;
    }

    public abstract Merchant getMerchant();

    protected abstract Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest);

    protected abstract Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body);

    protected abstract Object body(DetailsRequest detailsRequest);

    protected abstract Optional<DetailsResponse> buildResponse(T response);

    protected  <E extends Enum<E>> E parseMethod(String value, Class<E> methodType) {
        return EnumUtils.valueOf(methodType, value,
                () -> new MerchantMethodNotFoundException("Method \"" + value + "\" for merchant "
                + getMerchant().name() + " not found."));
    }
}
