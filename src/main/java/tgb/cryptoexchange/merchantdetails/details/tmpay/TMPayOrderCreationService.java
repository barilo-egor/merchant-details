package tgb.cryptoexchange.merchantdetails.details.tmpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.TMPayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class TMPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final TMPayProperties tmPayProperties;

    private final CallbackConfig callbackConfig;

    protected TMPayOrderCreationService(@Qualifier("tmPayWebClient") WebClient webClient,
                                        TMPayProperties tmPayProperties, ObjectMapper objectMapper,
                                        CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.tmPayProperties = tmPayProperties;
        this.objectMapper = objectMapper;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder
                .path("/invoice/create")
                .queryParam("partnerInvoiceId", UUID.randomUUID().toString())
                .queryParam("amount", detailsRequest.getAmount())
                .queryParam("invoiceType", Method.valueOf(detailsRequest.getMethod()).getValue())
                .queryParam("callbackUrl", callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                        + "&secret=" + callbackConfig.getCallbackSecret())
                .build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("apikey", tmPayProperties.key());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        return null;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getInvoiceId());
        detailsResponse.setMerchantOrderStatus(Status.CREATED.name());
        if (Objects.nonNull(response.getData().getPhone())) {
            detailsResponse.setDetails(response.getData().getBank() + " " + response.getData().getPhone());
        } else {
            detailsResponse.setDetails(response.getData().getBank() + " " + response.getData().getCard());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    protected HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.TM_PAY;
    }
}
