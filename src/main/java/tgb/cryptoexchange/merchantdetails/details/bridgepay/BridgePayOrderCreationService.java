package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.BridgePayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
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

    private final SleepingService sleepingService;

    protected BridgePayOrderCreationService(WebClient webClient, BridgePayProperties bridgePayProperties,
                                            SignatureService signatureService, CallbackConfig callbackConfig,
                                            SleepingService sleepingService) {
        super(webClient, Response.class, BridgeCallback.class);
        this.bridgePayProperties = bridgePayProperties;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
        this.sleepingService = sleepingService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/api/merchant/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return headers -> addHeaders(headers, parseMethod(request.getMethod(), Method.class), body,
                bridgePayProperties.url() + "/api/merchant/invoices");
    }

    private void addHeaders(HttpHeaders headers, Method method, String body, String url) {
        this.addHeaders(headers, method, body, url, HttpMethod.POST);
    }

    private void addHeaders(HttpHeaders headers, Method method, String body, String url, HttpMethod httpMethod) {
        headers.add("Content-Type", "application/json");
        headers.add("X-Identity", keyFunction().apply(method));
        try {
            headers.add("X-Signature", signatureService.hmacSHA1(
                    buildSignatureData(url, body, httpMethod), bridgePayProperties.secret()
            ));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Ошибка формирования подписи для method={}, url={}, body={}", method().name(), url, body);
            throw new SignatureCreationException("Ошибка формирования подписи.", e);
        }
    }

    protected Function<Method, String> keyFunction() {
        return method -> bridgePayProperties.key();
    }

    private String buildSignatureData(String url, String body, HttpMethod method) {
        return method.name().toUpperCase() + url + (body != null ? body : "");
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
        if (Arrays.asList(Method.SBP_QR, Method.MANUAL_SBP_QR).contains(dealDTO.getPaymentOption())) {
            requisiteVO.setQr(dealDTO.getQrCodeLink());
        } else {
            String details = dealDTO.getPaymentMethod().getDisplayName() + " " + dealDTO.getRequisites().getRequisites();
            requisiteVO.setDetails(details);
        }
        requisiteVO.setPaymentMethod(Objects.isNull(dealDTO.getPaymentOption()) ? null : dealDTO.getPaymentOption().name());
    }

    @Override
    protected Optional<String> makeRequest(OrderCreationRequest detailsRequest, String body) {
        Optional<String> createOrderResponse = super.makeRequest(detailsRequest, body);
        if (createOrderResponse.isEmpty()) {
            log.debug("Отсутствует тело ответа при создании ордера мерчанта {}.", getMerchant().name());
            return Optional.empty();
        }
        String createOrderResponseBody = createOrderResponse.get();
        Response response;
        try {
            response = objectMapper.readValue(createOrderResponseBody, Response.class);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа при создании ордера мерчанта {}, body: {}",
                    currentTime, getMerchant().name(), createOrderResponseBody);
            throw new ServiceUnavailableException("Error occurred while mapping create order response: " + currentTime);
        }
        if (CollectionUtils.isEmpty(response.getDeals()) || !Method.MANUAL_SBP_QR.equals(response.getDeals().getFirst().getPaymentOption())) {
            return Optional.of(createOrderResponseBody);
        }
        try {
            sleepingService.sleep(25);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при ожидании: {}", currentTime, e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while wait: " + currentTime);
        }
        String targetUrl = bridgePayProperties.url() + "/api/merchant/invoices/" + response.getId();
        String listOrderStringResponse = requestService.request(webClient, HttpMethod.GET,
                uriBuilder -> URI.create(targetUrl),
                headers -> addHeaders(headers, Method.MANUAL_SBP_QR, null, targetUrl, HttpMethod.GET),
                null
        );
        JsonNode listOrderResponse;
        try {
            listOrderResponse = objectMapper.readTree(listOrderStringResponse);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа списка ордеров мерчанта {}, body: {}", currentTime,
                    getMerchant().name(), createOrderResponseBody);
            throw new ServiceUnavailableException("Error occurred while mapping get order response: " + currentTime);
        }
        if (listOrderResponse != null && !listOrderResponse.isEmpty()) {
            return Optional.of(listOrderResponse.toPrettyString());
        }
        return Optional.empty();
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
        String cancelUrl = "/api/merchant/invoices/" + cancelOrderRequest.getOrderId() + "/cancel";
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path(cancelUrl).build(),
                headers -> addHeaders(headers, parseMethod(cancelOrderRequest.getMethod(), Method.class), null,
                        bridgePayProperties.url() + cancelUrl),
                null
        );
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("attachment", new ByteArrayResource(fileContent))
                .filename(fileName);

        URI fullUri = UriComponentsBuilder.fromUriString(bridgePayProperties.url())
                .pathSegment("api", "merchant", "invoices", orderId, "confirm-transfer").build().toUri();
        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> fullUri,
                headers -> {
                    addHeaders(headers, null, null, fullUri.toString());
                    headers.add("Content-Type", "multipart/form-data");
                },
                BodyInserters.fromMultipartData(builder.build()),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }
}
