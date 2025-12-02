package tgb.cryptoexchange.merchantdetails.details.appexbit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AppexbitProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class AppexbitOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final AppexbitProperties appexbitProperties;

    private final CallbackConfig callbackConfig;

    public AppexbitOrderCreationService(@Qualifier("appexbitWebClient") WebClient webClient,
                                        AppexbitProperties appexbitProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.appexbitProperties = appexbitProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.APPEXBIT;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/trade/createOffer").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("x-api-key", appexbitProperties.key());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmountFiat(detailsRequest.getAmount().toString());
        String callbackUrl = callbackConfig.getGatewayUrl()
                + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret();
        request.setGoodReturnLink(callbackUrl);
        request.setBadReturnLink(callbackUrl);
        request.setPaymentMethod(parseMethod(detailsRequest, Method.class));
        Request.FiatInfo fiatInfo = new Request.FiatInfo();
        request.setFiatInfo(fiatInfo);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.Offer offer = response.getAddedOffers().getFirst();
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(offer.getId());
        requisiteVO.setMerchantOrderStatus(offer.getStatus().name());
        requisiteVO.setDetails(offer.getMessage());
        return Optional.of(requisiteVO);
    }
}
