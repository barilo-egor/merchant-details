package tgb.cryptoexchange.merchantdetails.details.smackpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.SmackPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class SmackPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final SmackPayProperties smackPayProperties;

    protected SmackPayOrderCreationService(@Qualifier("smackPayWebClient") WebClient webClient,
                                           SmackPayProperties smackPayProperties) {
        super(webClient, Response.class, Callback.class);
        this.smackPayProperties = smackPayProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/payments").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + smackPayProperties.apiKey());
            httpHeaders.add("Idempotency-Key", UUID.randomUUID().toString());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());

        Request.Metadata metadata = new Request.Metadata();
        metadata.setOrderId(UUID.randomUUID().toString());
        metadata.setVisitorId(UUID.randomUUID().toString());
        request.setMetadata(metadata);

        request.setReturnUrl(detailsRequest.getRedirectUrl());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());

        detailsResponse.setDetails(response.getPaymentUrl());

        detailsResponse.setAmount(response.getAmount() != null ? response.getAmount() : 0);
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SMACK_PAY;
    }

}
