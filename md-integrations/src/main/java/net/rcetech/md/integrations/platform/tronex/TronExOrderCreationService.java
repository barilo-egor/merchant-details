package net.rcetech.md.integrations.platform.tronex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.BodyMappingException;
import net.rcetech.md.integrations.properties.TronExProperties;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class TronExOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final TronExProperties tronExProperties;

    protected final CallbackConfig callbackConfig;

    protected TronExOrderCreationService(RestClient restClient,
                                         TronExProperties tronExProperties, CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.tronExProperties = tronExProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/incoming/payment/create/").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("X-Api-Key", tronExProperties.key());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        request.setSourceBank(method.getSourceBank());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        Response.Data responseData = response.getPlatform();
        //По факту всегда будет Method.SBER_QR потому что ответ не возвращает source_bank, а bank_code возвращает vietcon (получателя) а не отправителя
        if (List.of(Method.SBER_QR, Method.ALFA_QR, Method.VTB_QR).contains(responseData.getMethod())) {
            detailsResponse.setQr(responseData.getPaymentUrl());
        } else {
            detailsResponse.setBank(responseData.getBank());
            detailsResponse.setDetails(responseData.getNumber());
        }
        detailsResponse.setMerchantOrderId(responseData.getId());
        detailsResponse.setAmount(Integer.valueOf(responseData.getAmount()));
        //Не возвращает статус в ответе
        detailsResponse.setMerchantOrderStatus(Status.CREATED.name());
        detailsResponse.setMerchant(getMerchant());
        return Optional.of(detailsResponse);
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String body;
        try {
            Map<String, String> bodyMap = Map.of("id", cancelOrderRequest.getOrderId());
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (JacksonException e) {
            throw new BodyMappingException("Ошибка сериализации JSON", e);
        }
        requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/incoming/payment/cancel").build(),
                this::addHeaders, body);

    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("id", orderId);
        builder.part("file", new ByteArrayResource(fileContent))
                .filename(fileName);

        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/payment/upload/check/").build(),
                headers -> headers.add("X-Api-Key", tronExProperties.key()),
                builder.build(),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }

}
