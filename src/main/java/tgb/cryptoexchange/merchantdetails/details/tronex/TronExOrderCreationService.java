package tgb.cryptoexchange.merchantdetails.details.tronex;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.properties.TronExProperties;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class TronExOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final TronExProperties tronExProperties;

    protected final CallbackConfig callbackConfig;

    protected TronExOrderCreationService(WebClient webClient,
                                         TronExProperties tronExProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.tronExProperties = tronExProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/incoming/payment/create/").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("X-Api-Key", tronExProperties.key());
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setMethod(method);
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        Response.Data responseData = response.getPlatform();
        if (Method.QR.equals(responseData.getMethod())) {
            detailsResponse.setQr(responseData.getPaymentUrl());
        } else {
            detailsResponse.setDetails(responseData.getBank() + " " + responseData.getNumber());
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
        } catch (JsonProcessingException e) {
            throw new BodyMappingException("Ошибка сериализации JSON", e);
        }
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/incoming/payment/cancel").build(),
                this::addHeaders, body);

    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("id", orderId);
        builder.part("file", new ByteArrayResource(fileContent))
                .filename(fileName);

        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/payment/upload/check/").build(),
                headers -> headers.add("X-Api-Key", tronExProperties.key()),
                BodyInserters.fromMultipartData(builder.build()),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }

}
