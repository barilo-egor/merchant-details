package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.EclipseGateConfig;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class EclipseGateService extends MerchantOrderCreationService<Response, Callback> {

    protected final EclipseGateConfig eclipseGateProperties;

    protected final CallbackConfig callbackConfig;

    protected EclipseGateService(WebClient webClient,
                                 EclipseGateConfig eclipseGateProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.eclipseGateProperties = eclipseGateProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/orders").build();
    }

    private void addHeaders(HttpHeaders httpHeaders, String method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("api-key", eclipseGateProperties.apiKey(method));
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> addHeaders(httpHeaders, detailsRequest.getCurrentMerchantMethod());
    }

    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setClientId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
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
        if (StringUtils.isNotBlank(requisites.getBill())) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getBill());
        } else {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getPhone());
        }
        return Optional.of(detailsResponse);
    }

}

