package tgb.cryptoexchange.merchantdetails.details.cashout;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.CashOutProperties;

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

    protected CashOutOrderCreationService(@Qualifier("cashOutWebClient") WebClient webClient,
            CashOutProperties cashOutProperties, ObjectMapper objectMapper) {
        super(webClient, Response.class, Callback.class);
        this.cashOutProperties = cashOutProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CASH_OUT;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path(CREATE_ORDER_URI).build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("accept", "application/json");
        httpHeaders.add("Authorization", "Bearer " + cashOutProperties.key());
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {

        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest, Method.class);
        request.setMethod(method);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getTransactionId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().getValue());
        Response.PaymentDetails paymentDetails = response.getData().getPaymentDetails();
        detailsResponse.setDetails(paymentDetails.getBankName() + " " + paymentDetails.getCardNumber());
        detailsResponse.setAmount(new BigDecimal(response.getData().getAmount()).intValue());
        return Optional.of(detailsResponse);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/merchants/transactions/{id}/cancel")
                        .build(cancelOrderRequest.getOrderId()),
                this::addHeaders,
                null
        );
    }

}
