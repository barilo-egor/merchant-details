package tgb.cryptoexchange.merchantdetails.details.studio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.StudioProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class StudioOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final StudioProperties studioProperties;

    private final CallbackConfig callbackConfig;

    protected StudioOrderCreationService(@Qualifier("studioWebClient") WebClient webClient,
            StudioProperties studioProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.studioProperties = studioProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/orders").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-Key", studioProperties.key());
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest, Method.class);
        request.setMainMethod(method.name());
        request.setClientOrderId(detailsRequest.getRequestId());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setAmount(Double.valueOf(response.getAmount()).intValue());
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

    @Override
    public Merchant getMerchant() {
        return Merchant.STUDIO;
    }

}
