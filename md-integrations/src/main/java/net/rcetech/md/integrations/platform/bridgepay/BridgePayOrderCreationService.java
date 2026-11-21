package net.rcetech.md.integrations.platform.bridgepay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.meta.exception.SignatureCreationException;
import net.rcetech.md.integrations.properties.BridgePayProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

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

    protected BridgePayOrderCreationService(RestClient restClient, BridgePayProperties bridgePayProperties,
                                            SignatureService signatureService, CallbackConfig callbackConfig,
                                            SleepingService sleepingService) {
        super(restClient, Response.class, BridgeCallback.class);
        this.bridgePayProperties = bridgePayProperties;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
        this.sleepingService = sleepingService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/merchant/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return headers -> addHeaders(headers, parseMethod(detailsRequest.getMethod(), Method.class), body,
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
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        request.setCurrency("RUB");
        request.setNotificationUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        request.setNotificationToken(bridgePayProperties.token());
        request.setInternalId(UUID.randomUUID().toString());
        request.setPaymentOption(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setStartDeal(true);
        return request;
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

    protected void fillRequisites(DetailsResponse requisiteVO, Response response) {
        DealDTO dealDTO = response.getDeals().getFirst();
        if (Arrays.asList(Method.SBP_QR, Method.MANUAL_SBP_QR).contains(dealDTO.getPaymentOption())) {
            requisiteVO.setQr(dealDTO.getQrCodeLink());
        } else {
            requisiteVO.setDetails(dealDTO.getRequisites().getRequisites());
            requisiteVO.setBank(dealDTO.getPaymentMethod());
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
        } catch (JacksonException e) {
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
        String listOrderStringResponse = requestService.request(restClient, HttpMethod.GET,
                uriBuilder -> URI.create(targetUrl),
                headers -> addHeaders(headers, Method.MANUAL_SBP_QR, null, targetUrl, HttpMethod.GET),
                null
        );
        JsonNode listOrderResponse;
        try {
            listOrderResponse = objectMapper.readTree(listOrderStringResponse);
        } catch (JacksonException e) {
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
            if (e instanceof HttpClientErrorException.BadRequest badRequest) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(badRequest.getResponseBodyAsString());
                } catch (JacksonException ex) {
                    return false;
                }
                if (response.isArray() && response.size() == 1) {
                    JsonNode node = response.get(0);
                    return node.has("message")
                            && node.get("message").asString().startsWith("Invoice amount should be");
                }
            }
            return false;
        };
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/api/merchant/invoices/" + cancelOrderRequest.getOrderId() + "/cancel";
        requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path(cancelUrl).build(),
                headers -> addHeaders(headers, parseMethod(cancelOrderRequest.getMethod(), Method.class), null,
                        bridgePayProperties.url() + cancelUrl),
                null
        );
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("attachment", new ByteArrayResource(fileContent))
                .filename(fileName);

        URI fullUri = UriComponentsBuilder.fromUriString(bridgePayProperties.url())
                .pathSegment("api", "merchant", "invoices", orderId, "confirm-transfer").build().toUri();
        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                uriBuilder -> fullUri,
                headers -> {
                    addHeaders(headers, null, null, fullUri.toString());
                    headers.add("Content-Type", "multipart/form-data");
                },
                builder.build(),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }
}
