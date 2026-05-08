package tgb.cryptoexchange.merchantdetails.details.studio;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.StudioConfig;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/orders").build();
    }

    private void addHeaders(HttpHeaders httpHeaders, String method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-Key", studioProperties.getKey(method));
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return httpHeaders -> addHeaders(httpHeaders, request.getMethod());
    }

    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setAmount(request.getAmount() * 100);
        Method method = parseMethod(request.getMethod(), Method.class);
        requestBody.setMainMethod(method);
        if (Method.SIM.equals(method)) {
            requestBody.setSubMethod("sim_sim");
        }
        requestBody.setClientOrderId(UUID.randomUUID().toString());
        requestBody.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        if (Objects.nonNull(response.getAmount())) {
            detailsResponse.setAmount(response.getAmount()/100);
        }
        detailsResponse.setMerchantOrderId(response.getInternalId());
        detailsResponse.setRequestId(response.getClientOrderId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        Response.Requisites requisites = response.getRequisites();
        if (requisites != null) {
            detailsResponse.setBank(requisites.getBankName());
            detailsResponse.setDetails(requisites.getAccount());
        }
        return Optional.of(detailsResponse);
    }

}
