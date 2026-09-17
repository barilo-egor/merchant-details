package tgb.cryptoexchange.merchantdetails.details.payscrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowProperties;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowTransgranProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PayscrowTransgranOrderCreationService extends MerchantOrderCreationService<ResponseTransgran, CallbackTransgran> {

    protected final String baseUrl;
    private final PayscrowProperties payscrowProperties;

    protected PayscrowTransgranOrderCreationService(@Qualifier("payscrowTransgranWebClient") WebClient webClient,
                                                    PayscrowTransgranProperties payscrowTransgranProperties,
                                                    @Value("${gateway-url}") String baseUrl) {
        super(webClient, ResponseTransgran.class, CallbackTransgran.class);
        this.payscrowProperties = payscrowTransgranProperties;
        this.baseUrl = baseUrl;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/form/create").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-Key", payscrowProperties.key());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        Request.OrderData orderData = new Request.OrderData();
        orderData.setAmount(detailsRequest.getAmount());
        orderData.setClientOrderId(UUID.randomUUID().toString());
        request.setOrderData(orderData);
        request.setRedirectUrl(detailsRequest.getRedirectUrl());
        request.setReturnUrl(detailsRequest.getRedirectUrl());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(ResponseTransgran response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        ResponseTransgran.Data data = response.getData();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(data.getOrderData().getOrderId());
        detailsResponse.setDetails(data.getFormUrl());
        detailsResponse.setAmount(data.getOrderData().getAmount().intValue());
        detailsResponse.setMerchantOrderStatus(Status.UNPAID.name());

        return Optional.of(detailsResponse);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_TRANSGRAN;
    }

}
