package tgb.cryptoexchange.merchantdetails.details.rspay;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.ReceiptPreparationException;
import tgb.cryptoexchange.merchantdetails.properties.RSPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

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

    protected RSPayOrderCreationService(WebClient webClient, RSPayProperties rsPayProperties,
                                        CallbackConfig callbackConfig, SignatureService signatureService) {
        super(webClient, Response.class, Callback.class);
        this.rsPayProperties = rsPayProperties;
        this.callbackConfig = callbackConfig;
        this.signatureService = signatureService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/requisites/request/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
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
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setTransactionId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount().toString());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
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
            detailsResponse.setDetails(requisites.getMobileProvider() + " " + requisites.getPhoneNumber());
        } else if (Objects.nonNull(requisites.getCardNumber())) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getCardNumber());
        } else {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getPhoneNumber());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/transactions/cancel/";
        try {
            Map<String, String> bodyMap = Map.of("transaction_id", cancelOrderRequest.getOrderId());
            String body = objectMapper.writeValueAsString(bodyMap);
            requestService.request(webClient, HttpMethod.POST,
                    uriBuilder -> uriBuilder.path(cancelUrl).build(),
                    httpHeaders -> addHeaders(httpHeaders, body),
                    body
            );
        } catch (JsonProcessingException e) {
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

            requestService.request(
                    webClient,
                    HttpMethod.POST,
                    uriBuilder -> uriBuilder.path("/requisites/receipt/").build(),
                    httpHeaders -> {
                        addReceiptHeaders(httpHeaders, rawMultipartBody);
                        httpHeaders.setContentType(MediaType.parseMediaType("multipart/form-data; boundary=" + fixedBoundary));
                    },
                    BodyInserters.fromValue(rawMultipartBody),
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
