package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.IDetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.SettleXProperties;

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

    protected SettleXOrderCreationService(WebClient webClient,
                                          SettleXProperties settleXProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.settleXProperties = settleXProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(IDetailsRequest detailsRequest, String merchantMethod) {
        return uriBuilder -> uriBuilder.path("/api/merchant/transactions/in").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(IDetailsRequest detailsRequest, String merchantMethod, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("x-merchant-api-key", settleXProperties.key());
        };
    }

    @Override
    protected Request body(IDetailsRequest detailsRequest, String merchantMethod) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(merchantMethod, Method.class);
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
            if (e instanceof WebClientResponseException.Conflict conflict) {
                try {
                    JsonNode response = objectMapper.readTree(conflict.getResponseBodyAsString());
                    return response.has("error")
                            && response.get("error").asText().equals("NO_REQUISITE");
                } catch (JsonProcessingException ex) {
                    return false;
                }
            }
            return false;
        };
    }
}
