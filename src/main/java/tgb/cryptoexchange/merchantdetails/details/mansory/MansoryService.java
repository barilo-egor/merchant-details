package tgb.cryptoexchange.merchantdetails.details.mansory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.MansoryProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class MansoryService extends MerchantOrderCreationService<Response, Callback> {

    protected final MansoryProperties mansoryProperties;

    protected final CallbackConfig callbackConfig;

    protected MansoryService(WebClient webClient,
                             MansoryProperties mansoryProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.mansoryProperties = mansoryProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/merchant/payment").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-Key", mansoryProperties.apiKey());
        httpHeaders.add("X-Secret-Key", mansoryProperties.secret());
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getOrderId());
        detailsResponse.setAmount(response.getAmount());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        Response.Requisites requisites = response.getRequisites();
        if (StringUtils.isNotBlank(requisites.getCardNumber())) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getCardNumber());
        } else {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getPhone());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/merchant/payment/" + cancelOrderRequest.getOrderId() + "/cancel").build(),
                this::addHeaders, null);
    }

}
