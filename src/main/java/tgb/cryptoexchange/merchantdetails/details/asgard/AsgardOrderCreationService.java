package tgb.cryptoexchange.merchantdetails.details.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.IDetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.AsgardProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

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

    protected AsgardOrderCreationService(WebClient webClient, AsgardProperties asgardProperties,
                                         SignatureService signatureService, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.asgardProperties = asgardProperties;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(IDetailsRequest detailsRequest, String merchantMethod) {
        return uriBuilder -> uriBuilder.path("/payments").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(IDetailsRequest detailsRequest, String merchantMethod, String body) {
        return headers -> addHeaders(headers, body);
    }

    private void addHeaders(HttpHeaders headers, String body) {
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + asgardProperties.token());
        headers.add("Signature", signatureService.hmacSHA256(body, asgardProperties.secret()));
    }

    @Override
    protected Request body(IDetailsRequest detailsRequest, String merchantMethod) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setMerchantId(asgardProperties.merchantId());
        request.setAmount(detailsRequest.getAmount());
        request.setCallbackUri(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        request.setMethod(parseMethod(merchantMethod, Method.class));
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
