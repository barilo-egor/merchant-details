package tgb.cryptoexchange.merchantdetails.details.buckspay;

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
import tgb.cryptoexchange.merchantdetails.properties.BucksPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class BucksPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final BucksPayProperties bucksPayProperties;

    protected final CallbackConfig callbackConfig;

    private final SignatureService signatureService;

    protected BucksPayOrderCreationService(WebClient webClient, BucksPayProperties bucksPayProperties,
                                           CallbackConfig callbackConfig, SignatureService signatureService) {
        super(webClient, Response.class, Callback.class);
        this.bucksPayProperties = bucksPayProperties;
        this.callbackConfig = callbackConfig;
        this.signatureService = signatureService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/invoices/set").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        Long nonce = new Date().getTime() * 1000;
        headers.add("NONCE", String.valueOf(nonce));
        headers.add("Content-Type", "application/json");
        headers.add("APIKEY", bucksPayProperties.key());
        headers.add("SIGNATURE", signatureService.hmacSHA256(
                bucksPayProperties.key() + nonce, bucksPayProperties.secret()
        ).toUpperCase());
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        request.setShop(bucksPayProperties.shopId());
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setPaymentType(method);
        request.setOperationId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        if (Objects.nonNull(response.getCardNumber())) {
            detailsResponse.setDetails(response.getBank().getName() + " " + response.getCardNumber());
        } else {
            detailsResponse.setDetails(response.getBank().getName() + " " + response.getPhoneNumber());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/invoice/" + cancelOrderRequest.getOrderId() + "/cancel";
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path(cancelUrl).build(),
                this::addHeaders,
                null
        );
    }

}
