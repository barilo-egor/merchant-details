package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantCallbackEvent;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.util.EnumUtils;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Сервис для выполнения запросов на создание ордера мерчантам.
 *
 * @param <T> тип ответа от мерчанта
 */
@Slf4j
public abstract class MerchantOrderCreationService<T extends MerchantDetailsResponse, P extends MerchantCallback> implements MerchantService {

    protected final WebClient webClient;

    private final Class<T> responseType;

    private final Class<P> callbackType;

    protected ObjectMapper objectMapper;

    protected RequestService requestService;

    protected KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate;

    private Environment environment;

    protected MerchantOrderCreationService(WebClient webClient, Class<T> responseType, Class<P> callbackType) {
        this.webClient = webClient;
        this.responseType = responseType;
        this.callbackType = callbackType;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    public void setRequestService(RequestService requestService) {
        this.requestService = requestService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired(required = false)
    public void setCallbackKafkaTemplate(@Qualifier("callbackKafkaTemplate") KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate) {
        this.callbackKafkaTemplate = callbackKafkaTemplate;
    }

    public Optional<DetailsResponse> createOrder(DetailsRequest detailsRequest) {
        log.debug("Запрос на создание ордера мерчанта {} метода {}: {}", getMerchant().name(), detailsRequest.getCurrentMerchantMethod(), detailsRequest);
        if (!isValidRequestPredicate().test(detailsRequest)) {
            log.debug("Запрос невалиден, ордер создан не будет. Запрос: {}, метод {}", detailsRequest, detailsRequest.getCurrentMerchantMethod());
            return Optional.empty();
        }
        String body = mapBody(detailsRequest);
        Optional<String> maybeRawResponse;
        try {
            maybeRawResponse = makeRequest(detailsRequest, body);
        } catch (Exception e) {
            if (isNoDetailsExceptionPredicate().test(e)) {
                logNoDetails(detailsRequest.getId(), detailsRequest.getCurrentMerchantMethod());
                return Optional.empty();
            }
            long currentTime = System.currentTimeMillis();
            handleRequestException(e, currentTime, detailsRequest, body);
            throw new ServiceUnavailableException("Error occurred while creating order: " + currentTime + ".", e);
        }
        if (maybeRawResponse.isEmpty()) {
            logNoDetails(detailsRequest.getId(), detailsRequest.getCurrentMerchantMethod());
            return Optional.empty();
        }
        String rawResponse = maybeRawResponse.get();
        if (hasResponseNoDetailsErrorPredicate().test(rawResponse)) {
            logNoDetails(detailsRequest.getId(), detailsRequest.getCurrentMerchantMethod());
            return Optional.empty();
        }
        T response = mapResponse(rawResponse);
        validateResponse(response, rawResponse);
        if (!response.hasDetails()) {
            logNoDetails(detailsRequest.getId(), detailsRequest.getCurrentMerchantMethod());
            return Optional.empty();
        }
        Optional<DetailsResponse> maybeResponse;
        try {
            maybeResponse = buildResponse(response);
        } catch (Exception e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при формировании ответа: {}", currentTime, e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while forming response: " + currentTime + ".", e);
        }
        if (maybeResponse.isPresent()) {
            log.debug("Реквизиты для id={} были найдены: {}", detailsRequest.getId(), maybeResponse.get());
        } else {
            logNoDetails(detailsRequest.getId(), detailsRequest.getCurrentMerchantMethod());
        }
        return maybeResponse;
    }

    private void logNoDetails(Long id, String method) {
        log.debug("Реквизиты для запроса id={} у мерчанта {}, метод {} получены не были.", id, getMerchant().name(), method);
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

    protected Optional<String> makeRequest(DetailsRequest detailsRequest, String body) {
        try {
            return Optional.ofNullable(requestService.request(
                    webClient, method(), uriBuilder(detailsRequest),
                    headers(detailsRequest, body), body
            ));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private void handleRequestException(Exception e, long currentTime, DetailsRequest detailsRequest, String body) {

        if (e instanceof WebClientResponseException webClientResponseException) {
            String errorBody = webClientResponseException.getResponseBodyAsString();
            log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (detailsRequest={}, body={}): {}, responseBody = {}",
                    currentTime, getMerchant().name(), detailsRequest.toString(), body, e.getMessage(),
                    errorBody, e);
        } else {
            log.error("{} Ошибка при попытке выполнения запроса к мерчанту {} (detailsRequest={}, body={}): {}",
                    currentTime, getMerchant().name(), detailsRequest.toString(), body, e.getMessage(), e);
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

    private void validateResponse(T response, String rawResponse) {
        ValidationResult validationResult = response.validate();
        if (!validationResult.isValid()) {
            long currentTime = System.currentTimeMillis();
            log.error("Ответ мерчанта {} невалиден: {}", getMerchant().name(), validationResult.errorsToString());
            log.error("Тело ответа: {}", rawResponse);
            throw new ServiceUnavailableException("Mapped response is invalid: " + currentTime);
        }
    }

    protected Predicate<DetailsRequest> isValidRequestPredicate() {
        return detailsRequest -> true;
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

    @Override
    public void updateStatus(String callbackBody) {
        log.debug("Принят callback от мерчанта {}: {}", getMerchant().name(), callbackBody);
        if (callbackBody.isBlank() || callbackBody.equals("{}")) {
            return;
        }
        P callback;
        try {
            callback = objectMapper.readValue(callbackBody, callbackType);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при попытке преобразования callback мерчанта {}. Тело callback: {}",
                    currentTime, getMerchant().name(), callbackBody, e);
            throw new ServiceUnavailableException("Error occurred while mapping callback: " + currentTime + ".", e);
        }
        Optional<String> maybeMerchantOrderId = callback.getMerchantOrderId();
        Optional<String> maybeStatus = callback.getStatusName();
        Optional<String> maybeStatusDescription = callback.getStatusDescription();
        if (maybeMerchantOrderId.isEmpty() || maybeStatus.isEmpty() || maybeStatusDescription.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Невалидный объект callback мерчанта {}: id is present {}, status is present {}, " +
                            "status description is present {}, body {}", currentTime, getMerchant().name(), maybeMerchantOrderId.isPresent(),
                    maybeStatus.isPresent(), maybeStatusDescription.isPresent(), callbackBody);
            throw new ServiceUnavailableException("Callback status and id must not be null: " + currentTime);
        }
        MerchantCallbackEvent merchantCallbackEvent = new MerchantCallbackEvent();
        merchantCallbackEvent.setMerchantOrderId(maybeMerchantOrderId.get());
        merchantCallbackEvent.setStatus(maybeStatus.get());
        merchantCallbackEvent.setStatusDescription(maybeStatusDescription.get());
        merchantCallbackEvent.setMerchant(getMerchant());
        if (Objects.nonNull(callbackKafkaTemplate)) {
            try {
                callbackKafkaTemplate.send(
                        environment.getRequiredProperty("kafka.topic.merchant-details.callback"),
                        UUID.randomUUID().toString(), merchantCallbackEvent
                );
            } catch (Exception e) {
                long currentTime = System.currentTimeMillis();
                log.error("{} Ошибка при попытке обновления статуса мерчанта {}. Callback объект={}, оригинальное тело={}. Message={}.",
                        currentTime, getMerchant().name(), callback, callbackBody, e.getMessage(), e);
                throw new ServiceUnavailableException("callback cannot be processed: " + currentTime);
            }
        }
    }

    public void cancelOrder(CancelOrderRequest cancelOrderRequest) {
        try {
            makeCancelRequest(cancelOrderRequest);
        } catch (Exception e) {
            log.error("Ошибка при попытке отмены ордера (cancelOrderRequest={}): {}", cancelOrderRequest, e.getMessage(), e);
        }
    }

    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        log.trace("Реализация отмены ордера для мерчанта {} отсутствует. Ордер {} не будет отменен.",
                getMerchant().name(), cancelOrderRequest.getOrderId());
    }

    public void sendReceipt(String orderId, MultipartFile multipartFile) {
        log.trace("Реализация отправки чека мерчанту {} отсутствует. Чек для ордера {} не будет отправлен.",
                getMerchant().name(), orderId);
    }
}
