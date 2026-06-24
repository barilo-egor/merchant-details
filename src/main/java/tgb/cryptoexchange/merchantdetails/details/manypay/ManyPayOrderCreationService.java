package tgb.cryptoexchange.merchantdetails.details.manypay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class ManyPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final ManyPayProperties manyPayProperties;

    protected final CallbackConfig callbackConfig;

    protected ManyPayOrderCreationService(WebClient webClient,
                                          ManyPayProperties manyPayProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.manyPayProperties = manyPayProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/merchant/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-ACCESS-TOKEN", manyPayProperties.token());
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount() * 100);
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setPaymentMethod(method);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        Response.Data data = response.getData();
        detailsResponse.setDetails(data.getPaymentDetails().getBankName() + " " + data.getPaymentDetails().getDetails());
        detailsResponse.setMerchantOrderId(data.getOrderId());
        detailsResponse.setMerchantOrderStatus(data.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        return Optional.of(detailsResponse);
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/api/order/" + cancelOrderRequest.getOrderId() + "/cancel").build(),
                this::addHeaders, null);
    }

}
