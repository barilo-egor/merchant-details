package net.rcetech.md.integrations.platform.settlex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.SettleXProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class SettleXOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final SettleXProperties settleXProperties;

    protected final CallbackConfig callbackConfig;

    protected SettleXOrderCreationService(RestClient restClient,
                                          SettleXProperties settleXProperties, CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.settleXProperties = settleXProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/merchant/transactions/in").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("x-merchant-api-key", settleXProperties.key());
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        if (Method.SBP.equals(method)) {
            request.setMethod(settleXProperties.sbpId());
        } else {
            request.setMethod(settleXProperties.c2cId());
        }
        request.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        request.setCallbackUri(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setBank(response.getRequisites().getBankName());
        detailsResponse.setDetails(response.getRequisites().getCardNumber());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantCustomId(response.getOrderId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof HttpClientErrorException.Conflict conflict) {
                try {
                    JsonNode response = objectMapper.readTree(conflict.getResponseBodyAsString());
                    return response.has("error")
                            && response.get("error").asString().equals("NO_REQUISITE");
                } catch (JacksonException ex) {
                    return false;
                }
            }
            return false;
        };
    }
}
