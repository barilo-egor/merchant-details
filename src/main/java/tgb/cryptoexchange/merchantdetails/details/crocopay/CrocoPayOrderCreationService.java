package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class CrocoPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final CrocoPayProperties crocoPayProperties;

    protected final CallbackConfig callbackConfig;

    protected CrocoPayOrderCreationService(WebClient webClient, CrocoPayProperties crocoPayProperties,
                                           CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.crocoPayProperties = crocoPayProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/api/v2/h2h/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return httpHeaders -> {
            httpHeaders.add("Client-Id", crocoPayProperties.clientId());
            httpHeaders.add("Client-Secret", crocoPayProperties.clientSecret());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        setCallback(request, detailsRequest);
        return request;
    }

    protected void setCallback(Request request, OrderCreationRequest detailsRequest) {
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback/" + getMerchant() + "?dealId="
                + detailsRequest.getId() + "&secret=" + callbackConfig.getCallbackSecret());
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.ResponseData responseData = response.getResponseData();
        DetailsResponse detailsResponse = new DetailsResponse();
        if ("any_rub_bank".equals(responseData.getPaymentRequisites().getPaymentMethod())) {
            detailsResponse.setDetails(responseData.getPaymentRequisites().getRequisites());
        } else {
            detailsResponse.setBank(responseData.getPaymentRequisites().getPaymentMethod());
            detailsResponse.setDetails(responseData.getPaymentRequisites().getRequisites());
        }

        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(responseData.getTransaction().getId());
        detailsResponse.setMerchantOrderStatus(responseData.getTransaction().getStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.InternalServerError internalServerError) {
                try {
                    JsonNode response = objectMapper.readTree(internalServerError.getResponseBodyAsString());
                    return response.has("code")
                            && response.get("code").asText().equals("REQUISITE_NOT_FOUND");
                } catch (JsonProcessingException ex) {
                    return false;
                }
            }
            return false;
        };
    }

}
