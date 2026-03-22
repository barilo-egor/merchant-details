package tgb.cryptoexchange.merchantdetails.details.fiatcut;

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
import tgb.cryptoexchange.merchantdetails.properties.FiatCutProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class FiatCutOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final FiatCutProperties fiatCutProperties;

    private final CallbackConfig callbackConfig;

    protected FiatCutOrderCreationService(@Qualifier("fiatCutWebClient") WebClient webClient,
                                          FiatCutProperties fiatCutProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.fiatCutProperties = fiatCutProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("Access-Token", fiatCutProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest, Method.class));
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        request.setExternalId(UUID.randomUUID().toString());
        request.setMerchantId(fiatCutProperties.merchantId());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getOrderId());
        detailsResponse.setDetails(response.getBankName() + " " + response.getPaymentDetail().getDetail());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.FIAT_CUT;
    }
}
