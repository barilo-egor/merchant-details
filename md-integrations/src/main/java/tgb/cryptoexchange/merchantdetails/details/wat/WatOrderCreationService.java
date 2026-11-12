package tgb.cryptoexchange.merchantdetails.details.wat;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.properties.WatProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class WatOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final WatProperties watProperties;

    protected final CallbackConfig callbackConfig;

    private final ReceiptService receiptService;

    protected WatOrderCreationService(RestClient restClient,
                                      WatProperties watProperties, CallbackConfig callbackConfig,
                                      ReceiptService receiptService) {
        super(restClient, Response.class, Callback.class);
        this.watProperties = watProperties;
        this.callbackConfig = callbackConfig;
        this.receiptService = receiptService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/orders").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + watProperties.token());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        Response.Data responseData = response.getData();
        detailsResponse.setBank(responseData.getBank().getName());
        detailsResponse.setDetails(responseData.getRequisite());
        detailsResponse.setMerchantOrderId(responseData.getId());
        detailsResponse.setMerchantOrderStatus(responseData.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        return Optional.of(detailsResponse);
    }

    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/orders/" + cancelOrderRequest.getOrderId() + "/cancel").build(),
                this::addHeaders, null);
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        String linkToReceipt = receiptService.saveReceipt(fileContent, fileName, StringUtils.lowerCase(getMerchant().name()));
        String body;
        try {
            Map<String, String> bodyMap = Map.of("receipt_url", linkToReceipt);
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (JacksonException e) {
            throw new BodyMappingException("Ошибка сериализации JSON", e);
        }

        requestService.request(
                restClient,
                HttpMethod.PATCH,
                uriBuilder -> uriBuilder.path("/orders/" + orderId).build(),
                this::addHeaders,
                body
        );
    }

    @Override
    public void deleteReceipt(String orderId, String orderStatus) {
        if (!Arrays.asList(Status.FINISHED.name(), Status.CANCELED.name()).contains(orderStatus)) {
            return;
        }
        String folderName = StringUtils.lowerCase(getMerchant().name());
        receiptService.deleteReceipt(orderId, folderName);
    }


}
