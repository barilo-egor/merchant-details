package tgb.cryptoexchange.merchantdetails.details.baypay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.BayPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class BayPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final BayPayProperties bayPayProperties;

    private final CallbackConfig callbackConfig;

    protected BayPayOrderCreationService(@Qualifier("bayPayWebClient") WebClient webClient,
                                         BayPayProperties bayPayProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.bayPayProperties = bayPayProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BAY_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/s2s/invoices/create/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + bayPayProperties.apiKey());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class));
        request.setOrderId(UUID.randomUUID().toString());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.Data data = response.getData();
        Response.Data.PaymentDetail paymentDetail = data.getPaymentDetail();
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderStatus(data.getStatus().name());
        requisiteVO.setMerchantOrderId(data.getId());
        requisiteVO.setAmount(Integer.valueOf(data.getAmount()));

        String requisite = paymentDetail.getBank() + " " + paymentDetail.getDetail();
        requisiteVO.setDetails(requisite);
        return Optional.of(requisiteVO);
    }

    @Override
    public void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String cancelUrl = "/s2s/invoice/" + cancelOrderRequest.getOrderId() + "/cancel/";
        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.path(cancelUrl).build(),
                headers(null, null),
                null
        );
    }

}
