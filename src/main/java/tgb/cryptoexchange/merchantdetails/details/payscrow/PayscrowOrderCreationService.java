package tgb.cryptoexchange.merchantdetails.details.payscrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

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

    private final PayscrowProperties payscrowProperties;

    protected PayscrowOrderCreationService(RestClient restClient, PayscrowProperties payscrowProperties) {
        super(restClient, Response.class, Callback.class);
        this.payscrowProperties = payscrowProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/order/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-Key", payscrowProperties.key());
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setClientOrderId(UUID.randomUUID().toString());
        request.setUniqueAmount(Merchant.PAYSCROW.equals(getMerchant()) ? true : null);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setBank(response.getMethodName());
        detailsResponse.setDetails(response.getHolderAccount());
        if (Objects.nonNull(response.getAmount())) {
            detailsResponse.setAmount(response.getAmount().intValue());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof HttpServerErrorException.InternalServerError ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isNoDetails(response) || isAmountError(response) || isInternalError(response);
                } catch (JacksonException jsonProcessingException) {
                    return false;
                }
            } else if (e instanceof HttpClientErrorException.Conflict ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isAmountError(response);
                } catch (JacksonException jsonProcessingException) {
                    return false;
                }
            }
            return false;
        };
    }

    private boolean isInternalError(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asString().equals("Internal server error");
    }

    private boolean isAmountError(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asString().contains("Amount for the chosen payment method doesn't meet limits.");
    }

    private boolean isNoDetails(JsonNode response) {
        return isNotSuccessAndHasMessage(response)
                && response.get(MESSAGE_FIELD).asString()
                .equals("No available traders that match order requirements. Please, try again later or change order parameters.");
    }

    private boolean isNotSuccessAndHasMessage(JsonNode response) {
        return response.has(SUCCESS_FIELD)
                && !response.get(SUCCESS_FIELD).asBoolean()
                && response.has(MESSAGE_FIELD);
    }
}
