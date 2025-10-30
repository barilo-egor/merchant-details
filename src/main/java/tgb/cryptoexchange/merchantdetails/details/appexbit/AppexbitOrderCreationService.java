package tgb.cryptoexchange.merchantdetails.details.appexbit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AppexbitProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class AppexbitOrderCreationService extends MerchantOrderCreationService<Response> {

    private final AppexbitProperties appexbitProperties;

    protected AppexbitOrderCreationService(@Qualifier("appexbitWebClient") WebClient webClient,
                                           AppexbitProperties appexbitProperties) {
        super(webClient, Response.class);
        this.appexbitProperties = appexbitProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.APPEXBIT;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        return uriBuilder -> uriBuilder.path("/trade/createOffer").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("x-api-key", appexbitProperties.key());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Object body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setAmountFiat(requisiteRequest.getAmount().toString());
        request.setGoodReturnLink(requisiteRequest.getCallbackUrl());
        request.setBadReturnLink(requisiteRequest.getCallbackUrl());
        request.setPaymentMethod(parseMethod(requisiteRequest.getMethod(), Method.class));
        Request.FiatInfo fiatInfo = new Request.FiatInfo();
        request.setFiatInfo(fiatInfo);
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if ((Objects.isNull(response.getSuccess()) || !response.getSuccess())
                || Objects.isNull(response.getAddedOffers()) || response.getAddedOffers().isEmpty()) {
            return Optional.empty();
        }
        Response.Offer offer = response.getAddedOffers().getFirst();
        RequisiteResponse requisiteVO = new RequisiteResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(offer.getId());
        requisiteVO.setMerchantOrderStatus(offer.getStatus().name());
        requisiteVO.setRequisite(offer.getMessage());
        return Optional.of(requisiteVO);
    }
}
