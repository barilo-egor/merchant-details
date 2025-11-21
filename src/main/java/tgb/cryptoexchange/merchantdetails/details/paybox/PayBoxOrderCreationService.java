package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PayBoxProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class PayBoxOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private static final String MESSAGE_FIELD = "message";

    private final PayBoxProperties payBoxProperties;

    protected PayBoxOrderCreationService(WebClient webClient, PayBoxProperties payBoxProperties) {
        super(webClient, Response.class, Callback.class);
        this.payBoxProperties = payBoxProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path("/api/v1/transactions" + method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + payBoxProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantTransactionId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        if (Objects.nonNull(response.getPhoneNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getPhoneNumber());
        } else {
            detailsResponse.setDetails(response.getBankName() + " " + response.getCardNumber());
        }
        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            try {
                if (e instanceof WebClientResponseException.InternalServerError ex) {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isUnableToGetRequisites(response) || isInternalServerError(response);
                } else if (e instanceof WebClientResponseException.UnprocessableEntity ex) {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    if (response.has("code") && response.get("code").asText().equals("422")
                            && response.has("errors")) {
                        JsonNode errors = response.get("errors");
                        if (errors.has("amount")) {
                            JsonNode amount = errors.get("amount");
                            return amount.isArray() && amount.size() == 1
                                    && amount.get(0).asText().startsWith("Amount should be");
                        }
                    }
                    return false;
                }
            } catch (JsonProcessingException jsonProcessingException) {
                return false;
            }
            return false;
        };
    }

    private boolean isInternalServerError(JsonNode response) {
        return response.has(MESSAGE_FIELD) && response.get(MESSAGE_FIELD).asText().equals("Internal Server Error");
    }

    private boolean isUnableToGetRequisites(JsonNode response) {
        return response.has("code")
                && response.get("code").asInt() == 1
                && response.has(MESSAGE_FIELD)
                && response.get(MESSAGE_FIELD).asText().equals("Unable to get requisites.");
    }
}
