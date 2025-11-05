package tgb.cryptoexchange.merchantdetails.details.paylee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayLeeProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class PayLeeMerchantService extends MerchantOrderCreationService<Response> {
    
    private final PayLeeProperties payLeeProperties;

    protected PayLeeMerchantService(@Qualifier("payLeeWebClient") WebClient webClient, PayLeeProperties payLeeProperties) {
        super(webClient, Response.class);
        this.payLeeProperties = payLeeProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/partners/purchases/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("Authorization", "Token " + payLeeProperties.token());
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setPrice(detailsRequest.getAmount());
        request.setRequisiteType(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse vo = new DetailsResponse();
        vo.setDetails(response.getBankName() + " " + response.getRequisites());
        vo.setMerchant(getMerchant());
        vo.setMerchantOrderId(response.getId().toString());
        vo.setMerchantOrderStatus(response.getStatus().name());
        vo.setAmount(response.getPrice().intValue());
        return Optional.of(vo);
    }
}
