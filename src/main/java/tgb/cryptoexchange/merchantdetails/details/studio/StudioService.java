package tgb.cryptoexchange.merchantdetails.details.studio;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.properties.StudioConfig;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StudioService extends MerchantOrderCreationService<Response, Callback> {

    protected final StudioConfig studioProperties;

    protected final CallbackConfig callbackConfig;

    protected StudioService(WebClient webClient,
            StudioConfig studioProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.studioProperties = studioProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/orders").build();
    }

    private void addHeaders(HttpHeaders httpHeaders, String method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-Key", studioProperties.getKey(method));
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        String method = detailsRequest.getMerchantMethod(getMerchant()).orElseThrow(
                () -> new MerchantMethodNotFoundException(
                        "Method for merchant " + getMerchant().name() + " not found."));
        return (httpHeaders) -> addHeaders(httpHeaders, method);
    }


    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount() * 100);
        Method method = parseMethod(detailsRequest, Method.class);
        request.setMainMethod(method);
        request.setClientOrderId(detailsRequest.getRequestId());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        if (Objects.nonNull(response.getAmount())) {
            detailsResponse.setAmount(response.getAmount());
        }
        detailsResponse.setMerchantOrderId(response.getInternalId());
        detailsResponse.setRequestId(response.getClientOrderId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        Response.Requisites requisites = response.getRequisites();
        if (requisites != null) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getBik());
        }
        return Optional.of(detailsResponse);
    }
}
