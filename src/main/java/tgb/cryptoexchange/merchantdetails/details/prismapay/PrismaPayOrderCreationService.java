package tgb.cryptoexchange.merchantdetails.details.prismapay;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.PrismaPayProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class PrismaPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final PrismaPayProperties prismaPayProperties;

    private final ReceiptService receiptService;

    protected PrismaPayOrderCreationService(@Qualifier("prismaPayWebClient") WebClient webClient,
                                            PrismaPayProperties prismaPayProperties, ReceiptService receiptService) {
        super(webClient, Response.class, Callback.class);
        this.prismaPayProperties = prismaPayProperties;
        this.receiptService = receiptService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/orders").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + prismaPayProperties.token());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(String.valueOf(response.getData().getId()));
        detailsResponse.setDetails(
                response.getData().getPaymentBank() + " " + response.getData().getPaymentDetails());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/api/orders/{orderId}/cancel")
                        .build(cancelOrderRequest.getOrderId()),
                this::addHeaders,
                null
        );
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        String linkToReceipt = receiptService.saveReceipt(fileContent, fileName, StringUtils.lowerCase(getMerchant().name()));
        try {
            Map<String, String> bodyMap = Map.of("link", linkToReceipt);
            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            requestService.request(
                    webClient,
                    HttpMethod.POST,
                    uriBuilder -> uriBuilder.pathSegment("api", "orders", "{orderId}", "merchant-document").build(orderId),
                    this::addHeaders,
                    jsonBody
            );
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации JSON для ордера {}: {}", orderId, e.getMessage(), e);
        }
    }

    @Override
    protected void deleteReceipt(String orderId, String orderStatus) {
        if (!Arrays.asList(Status.SUCCESS.name(), Status.CANCELLED.name()).contains(orderStatus)) {
            return;
        }
        String folderName = StringUtils.lowerCase(getMerchant().name());
        receiptService.deleteReceipt(orderId + ".pdf", folderName);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PRISMA_PAY;
    }

}
