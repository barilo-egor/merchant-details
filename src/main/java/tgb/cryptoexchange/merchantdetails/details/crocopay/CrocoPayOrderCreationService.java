package tgb.cryptoexchange.merchantdetails.details.crocopay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class CrocoPayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final CrocoPayProperties crocoPayProperties;

    protected CrocoPayOrderCreationService(@Qualifier("crocoPayWebClient") WebClient webClient,
                                           CrocoPayProperties crocoPayProperties) {
        super(webClient, Response.class);
        this.crocoPayProperties = crocoPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CROCO_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v2/h2h/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Client-Id", crocoPayProperties.clientId());
            httpHeaders.add("Client-Secret", crocoPayProperties.clientSecret());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.ResponseData responseData = response.getResponseData();
        String requisite;
        if ("any_rub_bank".equals(responseData.getPaymentRequisites().getPaymentMethod())) {
            requisite = responseData.getPaymentRequisites().getRequisites();
        } else {
            requisite = responseData.getPaymentRequisites().getPaymentMethod() + " " + responseData.getPaymentRequisites().getRequisites();
        }
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setDetails(requisite);
        detailsResponse.setMerchantOrderId(responseData.getTransaction().getId());
        detailsResponse.setMerchantOrderStatus(responseData.getTransaction().getStatus().name());
        return Optional.of(detailsResponse);
    }
}
