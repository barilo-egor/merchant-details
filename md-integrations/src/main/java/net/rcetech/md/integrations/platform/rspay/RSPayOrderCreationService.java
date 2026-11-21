package net.rcetech.md.integrations.platform.rspay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.BodyMappingException;
import net.rcetech.md.meta.exception.ReceiptPreparationException;
import net.rcetech.md.integrations.properties.RSPayProperties;
import net.rcetech.md.integrations.service.SignatureService;
import tools.jackson.core.JacksonException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class RSPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final RSPayProperties rsPayProperties;

    protected final CallbackConfig callbackConfig;

    private final SignatureService signatureService;

    protected RSPayOrderCreationService(RestClient restClient, RSPayProperties rsPayProperties,
                                        CallbackConfig callbackConfig, SignatureService signatureService) {
        super(restClient, Response.class, Callback.class);
        this.rsPayProperties = rsPayProperties;
        this.callbackConfig = callbackConfig;
        this.signatureService = signatureService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/requisites/request/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return headers -> addHeaders(headers, body);
    }

    private void addHeaders(HttpHeaders headers, String body) {
        String signature = signatureService.hmacSHA256(body, rsPayProperties.secret());
        headers(headers, signature);
        headers.add("Content-Type", "application/json");
    }

    private void addReceiptHeaders(HttpHeaders headers, byte[] body) {
        String signature = signatureService.hmacSHA256(body, rsPayProperties.secret());
        headers(headers, signature);
    }

    private void headers(HttpHeaders headers, String signature) {
        long timestamp = Instant.now().toEpochMilli();
        long nonce = timestamp * 1000;

        headers.add("X-Nonce", String.valueOf(nonce));
        headers.add("X-Timestamp", String.valueOf(timestamp));
        headers.add("X-Shop-API-Key", rsPayProperties.apiKey());
        headers.add("X-Signature", signature);
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setTransactionId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount().toString());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setPaymentMethod(method);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        Response.Requisites requisites = response.getRequisites();
        if (Objects.nonNull(requisites.getPaymentLink())) {
            detailsResponse.setQr(requisites.getPaymentLink());
        } else if (Objects.nonNull(requisites.getMobileProvider())) {
            detailsResponse.setBank(requisites.getMobileProvider());
            detailsResponse.setDetails(requisites.getPhoneNumber());
        } else {
            detailsResponse.setBank(requisites.getBankName());
            if (Objects.nonNull(requisites.getCardNumber())) {
                detailsResponse.setDetails(requisites.getCardNumber());
            } else {
                detailsResponse.setDetails(requisites.getPhoneNumber());
            }
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/transactions/cancel/";
        try {
            Map<String, String> bodyMap = Map.of("transaction_id", cancelOrderRequest.getOrderId());
            String body = objectMapper.writeValueAsString(bodyMap);
            requestService.request(restClient, HttpMethod.POST,
                    uriBuilder -> uriBuilder.path(cancelUrl).build(),
                    httpHeaders -> addHeaders(httpHeaders, body),
                    body
            );
        } catch (JacksonException e) {
            throw new BodyMappingException("Ошибка сериализации JSON", e);
        }
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        try {
            String fixedBoundary = UUID.randomUUID().toString();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);

            writeMultipartLine(out, "--" + fixedBoundary);
            writeMultipartLine(out, "Content-Disposition: form-data; name=\"transaction_id\"");
            writeMultipartLine(out, "");
            writeMultipartLine(out, orderId);

            writeMultipartLine(out, "--" + fixedBoundary);
            writeMultipartLine(out, "Content-Disposition: form-data; name=\"proof\"; filename=\"" + fileName + "\"");
            writeMultipartLine(out, "Content-Type: application/pdf");
            writeMultipartLine(out, "");
            out.write(fileContent);
            out.write(crlf);

            writeMultipartLine(out, "--" + fixedBoundary + "--");

            byte[] rawMultipartBody = out.toByteArray();

            requestService.requestAsync(
                    restClient,
                    HttpMethod.POST,
                    uriBuilder -> uriBuilder.path("/requisites/receipt/").build(),
                    httpHeaders -> {
                        addReceiptHeaders(httpHeaders, rawMultipartBody);
                        httpHeaders.setContentType(MediaType.parseMediaType("multipart/form-data; boundary=" + fixedBoundary));
                    },
                    rawMultipartBody,
                    t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}",
                            getMerchant().getDisplayName(), orderId, t.getMessage(), t)
            );

        } catch (Exception e) {
            log.error("Ошибка подготовки multipart-тела для ордера {}", orderId, e);
            throw new ReceiptPreparationException("Ошибка подготовки чека multipart-тела", e);
        }
    }

    private void writeMultipartLine(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

}
