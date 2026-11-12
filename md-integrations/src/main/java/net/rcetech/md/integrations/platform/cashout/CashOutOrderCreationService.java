package net.rcetech.md.integrations.platform.cashout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.CashOutProperties;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class CashOutOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private static final String CREATE_ORDER_URI = "/merchants/transactions/create-manual";

    private final CashOutProperties cashOutProperties;

    protected CashOutOrderCreationService(@Qualifier("cashOutRestClient") RestClient restClient,
            CashOutProperties cashOutProperties, ObjectMapper objectMapper) {
        super(restClient, Response.class, Callback.class);
        this.cashOutProperties = cashOutProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CASH_OUT;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path(CREATE_ORDER_URI).build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("accept", "application/json");
        httpHeaders.add("Authorization", "Bearer " + cashOutProperties.key());
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {

        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getTransactionId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        Response.PaymentDetails paymentDetails = response.getData().getPaymentDetails();
        detailsResponse.setBank(paymentDetails.getBankName());
        detailsResponse.setDetails(paymentDetails.getCardNumber());
        detailsResponse.setAmount(new BigDecimal(response.getData().getAmount()).intValue());
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/merchants/transactions/{id}/cancel")
                        .build(cancelOrderRequest.getOrderId()),
                this::addHeaders,
                null
        );
    }

}
