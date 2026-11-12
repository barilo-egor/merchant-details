package net.rcetech.md.integrations.platform.buckspay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.BucksPayProperties;
import net.rcetech.md.integrations.service.SignatureService;

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

    protected BucksPayOrderCreationService(RestClient restClient, BucksPayProperties bucksPayProperties,
                                           CallbackConfig callbackConfig, SignatureService signatureService) {
        super(restClient, Response.class, Callback.class);
        this.bucksPayProperties = bucksPayProperties;
        this.callbackConfig = callbackConfig;
        this.signatureService = signatureService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/invoices/set").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        return headers -> addHeaders(headers, method);
    }

    private void addHeaders(HttpHeaders headers, Method method) {
        Long nonce = new Date().getTime() * 1000;
        headers.add("NONCE", String.valueOf(nonce));
        headers.add("Content-Type", "application/json");
        headers.add("APIKEY", getApiKey(method));
        headers.add("SIGNATURE", signatureService.hmacSHA256(
                getApiKey(method) + nonce, getSecret(method)
        ).toUpperCase());
    }

    private String getApiKey(Method method) {
        return switch (method) {
            case NSPK -> bucksPayProperties.qrKey();
            case T_PAY -> bucksPayProperties.tPayKey();
            default -> bucksPayProperties.key();
        };
    }

    private String getSecret(Method method) {
        return switch (method) {
            case NSPK -> bucksPayProperties.qrSecret();
            case T_PAY -> bucksPayProperties.tPaySecret();
            default -> bucksPayProperties.secret();
        };
    }

    private String getShopId(Method method) {
        return switch (method) {
            case NSPK -> bucksPayProperties.qrShopId();
            case T_PAY -> bucksPayProperties.tPayShopId();
            default -> bucksPayProperties.shopId();
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setShop(getShopId(method));
        request.setPaymentType(method);
        request.setOperationId(UUID.randomUUID().toString());
        request.setBank(method.getBankCode());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        if (Objects.nonNull(response.getQrLink())) {
            detailsResponse.setQr(response.getQrLink());
        } else {
            detailsResponse.setBank(response.getBank().getName());
            if (Objects.nonNull(response.getCardNumber())) {
                detailsResponse.setDetails(response.getCardNumber());
            } else {
                detailsResponse.setDetails(response.getPhoneNumber());
            }
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/invoice/" + cancelOrderRequest.getOrderId() + "/cancel";
        Method method = parseMethod(cancelOrderRequest.getMethod(), Method.class);
        requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path(cancelUrl).build(),
                httpHeaders -> addHeaders(httpHeaders, method),
                null
        );
    }

}
