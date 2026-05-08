package tgb.cryptoexchange.merchantdetails.details.noros;

import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.NorosProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
public abstract class NorosOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final NorosProperties norosProperties;

    private final Hashids hashids;

    protected NorosOrderCreationService(WebClient webClient, NorosProperties norosProperties) {
        super(webClient, Response.class, Callback.class);
        this.norosProperties = norosProperties;
        this.hashids = new Hashids(norosProperties.clientIdSalt(), 8);
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/transaction").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("api_key", norosProperties.key());
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return this::addHeaders;
    }

    @Override
    public Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setOrderId(UUID.randomUUID().toString());
        requestBody.setAmount(request.getAmount());
        requestBody.setPaymentMethod(parseMethod(request.getMethod(), Method.class));
        if (Objects.nonNull(request.getUserId())) {
            requestBody.setClientId(hashids.encode(Long.parseLong(request.getUserId())));
        }
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setBank(response.getBankReceiver());
        detailsResponse.setDetails(response.getCard());
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
