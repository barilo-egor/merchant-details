package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.BridgePayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class BridgePayOrderCreationService extends MerchantOrderCreationService<Response, BridgeCallback> {

    private final BridgePayProperties bridgePayProperties;

    private final SignatureService signatureService;

    private final CallbackConfig callbackConfig;

    protected BridgePayOrderCreationService(WebClient webClient, BridgePayProperties bridgePayProperties,
                                            SignatureService signatureService, CallbackConfig callbackConfig) {
        super(webClient, Response.class, BridgeCallback.class);
        this.bridgePayProperties = bridgePayProperties;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/api/merchant/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return headers -> addHeaders(headers, parseMethod(request.getMethod(), Method.class), body);
    }

    private void addHeaders(HttpHeaders headers, Method method, String body) {
        headers.add("Content-Type", "application/json");
        headers.add("X-Identity", keyFunction().apply(method));
        String createInvoiceUrl = bridgePayProperties.url() + "/api/merchant/invoices";
        try {
            headers.add("X-Signature", signatureService.hmacSHA1(
                    buildSignatureData(createInvoiceUrl, body), bridgePayProperties.secret()
            ));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Ошибка формирования подписи для method={}, url={}, body={}", method().name(), createInvoiceUrl, body);
            throw new SignatureCreationException("Ошибка формирования подписи.", e);
        }
    }

    protected Function<Method, String> keyFunction() {
        return method -> bridgePayProperties.key();
    }

    private String buildSignatureData(String url, String body) {
        return method().name().toUpperCase() + url + (body != null ? body : "");
    }

    @Override
    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setAmount(request.getAmount().toString());
        requestBody.setCurrency(FiatCurrency.RUB.name());
        requestBody.setNotificationUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        requestBody.setNotificationToken(bridgePayProperties.token());
        requestBody.setInternalId(UUID.randomUUID().toString());
        requestBody.setPaymentOption(parseMethod(request.getMethod(), Method.class));
        requestBody.setStartDeal(true);
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        String invoiceId = response.getId();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(invoiceId);
        requisiteVO.setMerchantOrderStatus(Status.NEW.name());
        fillRequisites(requisiteVO, response);
        return Optional.of(requisiteVO);
    }

    private void fillRequisites(DetailsResponse requisiteVO, Response response) {
        DealDTO dealDTO = response.getDeals().getFirst();
        if (Method.SBP_QR.equals(dealDTO.getPaymentOption())) {
            requisiteVO.setQr(dealDTO.getQrCodeLink());
        } else {
            String details = dealDTO.getPaymentMethod().getDisplayName() + " " + dealDTO.getRequisites().getRequisites();
            requisiteVO.setDetails(details);
        }
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.BadRequest badRequest) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(badRequest.getResponseBodyAsString());
                } catch (JsonProcessingException ex) {
                    return false;
                }
                if (response.isArray() && response.size() == 1) {
                    JsonNode node = response.get(0);
                    return node.has("message")
                            && node.get("message").asText().startsWith("Invoice amount should be");
                }
            }
            return false;
        };
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/api/merchant/invoices/" + cancelOrderRequest.getOrderId() + "/cancel").build(),
                headers -> addHeaders(headers, parseMethod(cancelOrderRequest.getMethod(), Method.class), null),
                null
        );
    }
}
