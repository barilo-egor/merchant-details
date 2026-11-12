package net.rcetech.md.integrations.platform.levelpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.LevelPayProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class LevelPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final CallbackConfig callbackConfig;

    private final LevelPayProperties levelPayProperties;

    protected LevelPayOrderCreationService(RestClient restClient, CallbackConfig callbackConfig,
                                           LevelPayProperties levelPayProperties) {
        super(restClient, Response.class, Callback.class);
        this.callbackConfig = callbackConfig;
        this.levelPayProperties = levelPayProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Access-Token", levelPayProperties.token());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setMerchantId(levelPayProperties.merchantId());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setPaymentDetailType(method);
        if (Method.ALFA_ALFA.equals(method)) {
            request.setPaymentGateway("alfa-alfa");
        }
        request.setExternalId(UUID.randomUUID().toString());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        request.setFloatingAmount(true);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        if (Objects.nonNull(response.getData().getPaymentDetail().getDetail())) {
            String bank = Objects.nonNull(response.getData().getPaymentGatewayName())
                    ? response.getData().getPaymentGatewayName()
                    : response.getData().getPaymentGateway();
            detailsResponse.setBank(bank);
            detailsResponse.setDetails(response.getData().getPaymentDetail().getDetail());
        } else {
            detailsResponse.setQr(response.getData().getPaymentDetail().getQrCodeLink());
        }
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getOrderId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        detailsResponse.setAmount(getAmount(response.getData().getAmount()));
        return Optional.of(detailsResponse);
    }

    protected Integer getAmount(String amount) {
        return Integer.parseInt(amount);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof HttpClientErrorException.BadRequest ex) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(ex.getResponseBodyAsString());
                } catch (JacksonException exc) {
                    return false;
                }
                return response.has("success") && !response.get("success").asBoolean();
            }
            return false;
        };
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(restClient, HttpMethod.PATCH,
                uriBuilder -> uriBuilder.path("/api/h2h/order/" + cancelOrderRequest.getOrderId() + "/cancel").build(),
                this::addHeaders, null);
    }
}
