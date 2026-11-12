package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class CrocoPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final CrocoPayProperties crocoPayProperties;

    protected final CallbackConfig callbackConfig;

    protected CrocoPayOrderCreationService(RestClient restClient, CrocoPayProperties crocoPayProperties,
                                           CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.crocoPayProperties = crocoPayProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v2/h2h/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
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
            if (e instanceof HttpServerErrorException.InternalServerError internalServerError) {
                try {
                    JsonNode response = objectMapper.readTree(internalServerError.getResponseBodyAsString());
                    return response.has("code")
                            && response.get("code").asString().equals("REQUISITE_NOT_FOUND");
                } catch (JacksonException ex) {
                    return false;
                }
            }
            return false;
        };
    }

}
