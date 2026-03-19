package tgb.cryptoexchange.merchantdetails.details.noros;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.payscrow.Callback;
import tgb.cryptoexchange.merchantdetails.properties.NorosProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
public abstract class NorosOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final NorosProperties norosProperties;

    protected NorosOrderCreationService(WebClient webClient, NorosProperties norosProperties) {
        super(webClient, Response.class, Callback.class);
        this.norosProperties = norosProperties;
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/transaction").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("api_key", norosProperties.key());
    }

    @Override
    public Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    public Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest, Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setDetails(response.getBankReceiver() + " " + response.getCard());
        detailsResponse.setAmount(response.getAmount());

        return Optional.of(detailsResponse);
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.DELETE,
                uriBuilder -> uriBuilder.path("/transaction/" + cancelOrderRequest.getOrderId()).build(),
                this::addHeaders, null);
    }

}
