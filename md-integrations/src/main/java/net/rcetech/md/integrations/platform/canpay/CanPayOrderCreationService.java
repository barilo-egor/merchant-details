package net.rcetech.md.integrations.platform.canpay;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.CanPayProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class CanPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final CanPayProperties canPayProperties;
    private final CanPayClientIds clientIds;

    protected CanPayOrderCreationService(@Qualifier("canPayRestClient") RestClient restClient,
                                         CanPayProperties canPayProperties,
                                         CanPayClientIds clientIds) {
        super(restClient, Response.class, Callback.class);
        this.canPayProperties = canPayProperties;
        this.clientIds = clientIds;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/payments").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + canPayProperties.apiKey());
            httpHeaders.add("Idempotency-Key", UUID.randomUUID().toString());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setPhone(detailsRequest.getPhone());
        if (detailsRequest.getUserId() != null) {
            clientIds.findOrAssignClientId(detailsRequest.getUserId())
                    .ifPresent(request::setClientId);
        }

        Request.Metadata metadata = new Request.Metadata();
        metadata.setOrderId(UUID.randomUUID().toString());
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

        detailsResponse.setAmount(response.getAmount());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CANPAY;
    }

}
