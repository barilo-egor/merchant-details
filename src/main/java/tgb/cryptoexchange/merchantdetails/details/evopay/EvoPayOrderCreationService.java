package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallbackMock;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.EvoPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Slf4j
public class EvoPayOrderCreationService extends MerchantOrderCreationService<Response, MerchantCallbackMock> {

    private static final String ENTRIES_FIELD = "entries";

    private final EvoPayProperties evoPayProperties;

    private final WebClient webClient;

    private final SleepingService sleepingService;

    protected EvoPayOrderCreationService(@Qualifier("evoPayWebClient") WebClient webClient,
                                         EvoPayProperties evoPayProperties, SleepingService sleepingService) {
        super(webClient, Response.class, MerchantCallbackMock.class);
        this.evoPayProperties = evoPayProperties;
        this.webClient = webClient;
        this.sleepingService = sleepingService;
    }

    @Override
    protected Optional<String> makeRequest(DetailsRequest detailsRequest, String body) {
        Optional<String> createOrderResponse = super.makeRequest(detailsRequest, body);
        if (createOrderResponse.isEmpty()) {
            log.debug("Отсутствует тело ответа при создании ордера мерчанта {}.", getMerchant().name());
            return Optional.empty();
        }
        String createOrderResponseBody = createOrderResponse.get();
        Response response;
        try {
             response = objectMapper.readValue(createOrderResponseBody, Response.class);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа при создании ордера мерчанта {}, body: {}",
                    currentTime, getMerchant().name(), createOrderResponseBody);
            throw new ServiceUnavailableException("Error occurred while mapping create order response: " + currentTime);
        }
        try {
            sleepingService.sleep(8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при ожидании: {}", currentTime, e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while wait: " + currentTime);
        }
        String listOrderStringResponse = requestService.request(
                webClient,
                HttpMethod.GET,
                uriBuilder -> uriBuilder.path("/v1/api/order/list").queryParam("order_id", response.getId()).build(),
                this.headers(detailsRequest, body),
                body
        );
        JsonNode listOrderResponse;
        try {
            listOrderResponse = objectMapper.readTree(listOrderStringResponse);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа списка ордеров мерчанта {}, body: {}", currentTime,
                    getMerchant().name(), createOrderResponseBody);
            throw new ServiceUnavailableException("Error occurred while mapping get order response: " + currentTime);
        }
        if (listOrderResponse.has(ENTRIES_FIELD)) {
            JsonNode entries = listOrderResponse.get(ENTRIES_FIELD);
            if (entries.isArray() && !entries.isEmpty()) {
                return Optional.of(listOrderResponse.get(ENTRIES_FIELD).get(0).toPrettyString());
            }
        }
        return Optional.empty();
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EVO_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/v1/api/order/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("x-api-key", getKey(detailsRequest.getAmount()));
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    private String getKey(Integer amount) {
        if (amount.compareTo(1000) > 0) {
            return evoPayProperties.key();
        } else {
            return evoPayProperties.changeKey();
        }
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setCustomId(UUID.randomUUID().toString());
        request.setFiatSum(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.EVO_PAY);
        if (Objects.nonNull(response.getRequisites().getRecipientCardNumber())
                && !response.getRequisites().getRecipientCardNumber().isBlank()) {
            detailsResponse.setDetails(
                    response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientCardNumber()
            );
        } else {
            detailsResponse.setDetails(
                    response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientPhoneNumber()
            );
        }
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getOrderStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return exception -> {
            if (exception instanceof WebClientResponseException.InternalServerError internalServerError) {
                return "Internal Server Error".equals(internalServerError.getResponseBodyAsString());
            }
            return exception instanceof WebClientResponseException.BadGateway;
        };
    }
}
