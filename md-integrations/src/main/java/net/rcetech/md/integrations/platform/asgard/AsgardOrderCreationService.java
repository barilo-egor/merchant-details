package net.rcetech.md.integrations.platform.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.AsgardProperties;
import net.rcetech.md.integrations.service.SignatureService;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class AsgardOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final AsgardProperties asgardProperties;

    private final SignatureService signatureService;

    private final CallbackConfig callbackConfig;

    protected AsgardOrderCreationService(RestClient restClient, AsgardProperties asgardProperties,
                                         SignatureService signatureService, CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.asgardProperties = asgardProperties;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/payments").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return headers -> addHeaders(headers, body);
    }

    private void addHeaders(HttpHeaders headers, String body) {
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + asgardProperties.token());
        headers.add("Signature", signatureService.hmacSHA256(body, asgardProperties.secret()));
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setMerchantId(asgardProperties.merchantId());
        request.setAmount(detailsRequest.getAmount());
        request.setCallbackUri(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        Response.Requisites requisites = response.getRequisites();
        detailsResponse.setMerchantOrderId(requisites.getId());
        detailsResponse.setMerchantOrderStatus(requisites.getState().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(requisites.getAmount().intValue());
        Method method = requisites.getMethod();
        detailsResponse.setPaymentMethod(method.name());
        detailsResponse.setBank(requisites.getBankName());
        detailsResponse.setDetails(requisites.getAddress());
        return Optional.of(detailsResponse);
    }

}
