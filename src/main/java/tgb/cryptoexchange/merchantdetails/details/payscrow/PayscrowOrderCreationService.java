package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class PayscrowOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private static final String SUCCESS_FIELD = "success";

    private static final String MESSAGE_FIELD = "message";

    private final PayscrowProperties payscrowPropertiesImpl;

    protected PayscrowOrderCreationService(WebClient webClient, PayscrowProperties payscrowProperties) {
        super(webClient, Response.class, Callback.class);
        this.payscrowPropertiesImpl = payscrowProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/order/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-Key", keyFunction().apply(detailsRequest));
        };
    }

    public Function<DetailsRequest, String> keyFunction() {
        return method -> payscrowPropertiesImpl.key();
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest, Method.class));
        request.setClientOrderId(UUID.randomUUID().toString());
        request.setUniqueAmount(Merchant.PAYSCROW.equals(getMerchant()) ? true : null);
        return request;
    }

    protected abstract Boolean getUniqueAmount();

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setDetails(response.getMethodName() + " " + response.getHolderAccount());
        if (Objects.nonNull(response.getAmount())) {
            detailsResponse.setAmount(response.getAmount().intValue());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.InternalServerError ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isNoDetails(response) || isAmountError(response) || isInternalError(response);
                } catch (JsonProcessingException jsonProcessingException) {
                    return false;
                }
            } else if (e instanceof WebClientResponseException.Conflict ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isAmountError(response);
                } catch (JsonProcessingException jsonProcessingException) {
                    return false;
                }
            }
            return false;
        };
    }

    private boolean isInternalError(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asText().equals("Internal server error");
    }

    private boolean isAmountError(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asText().contains("Amount for the chosen payment method doesn't meet limits.");
    }

    private boolean isNoDetails(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asText()
                .equals("No available traders that match order requirements. Please, try again later or change order parameters.");
    }

    private boolean isNotSuccessAndHasMessage(JsonNode response) {
        return response.has(SUCCESS_FIELD)
                && !response.get(SUCCESS_FIELD).asBoolean()
                && response.has(MESSAGE_FIELD);
    }
}
