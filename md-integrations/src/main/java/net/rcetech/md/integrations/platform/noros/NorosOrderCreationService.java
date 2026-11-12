package net.rcetech.md.integrations.platform.noros;

import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.NorosProperties;
import net.rcetech.md.integrations.util.ClientIdUtils;

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

    protected NorosOrderCreationService(RestClient restClient, NorosProperties norosProperties) {
        super(restClient, Response.class, Callback.class);
        this.norosProperties = norosProperties;
        this.hashids = new Hashids(norosProperties.clientIdSalt(), 8);
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/transaction").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("api_key", norosProperties.key());
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    public Request body(OrderCreationRequest detailsRequest) {
        Request requestBody = new Request();
        requestBody.setOrderId(UUID.randomUUID().toString());
        requestBody.setAmount(detailsRequest.getAmount());
        requestBody.setPaymentMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        if (Objects.nonNull(detailsRequest.getUserId())) {
            requestBody.setClientId(ClientIdUtils.encode(hashids, detailsRequest.getUserId()));
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
        requestService.request(restClient, HttpMethod.DELETE,
                uriBuilder -> uriBuilder.path("/transaction/" + cancelOrderRequest.getOrderId()).build(),
                this::addHeaders, null);
    }

}
