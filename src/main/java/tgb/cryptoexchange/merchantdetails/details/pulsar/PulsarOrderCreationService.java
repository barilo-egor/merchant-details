package tgb.cryptoexchange.merchantdetails.details.pulsar;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PulsarProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class PulsarOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PulsarProperties pulsarProperties;

    private final SignatureService signatureService;

    protected PulsarOrderCreationService(@Qualifier("pulsarWebClient") WebClient webClient,
                                         PulsarProperties pulsarProperties, SignatureService signatureService) {
        super(webClient, Response.class);
        this.pulsarProperties = pulsarProperties;
        this.signatureService = signatureService;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PULSAR;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v2/payments").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + pulsarProperties.token());
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Signature", signatureService.hmacSHA256(body, pulsarProperties.secret()));
        };
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantId(pulsarProperties.code());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setOrderId(UUID.randomUUID().toString());
        request.setUserId(detailsRequest.getChatId().toString());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getResult())
                || Objects.isNull(response.getResult().getAddress())
                || Objects.isNull(response.getResult().getBankName())) {
            return Optional.empty();
        }
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(response.getResult().getBankName() + " " + response.getResult().getAddress());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getResult().getId());
        detailsResponse.setMerchantOrderStatus(response.getResult().getState().name());
        return Optional.of(detailsResponse);
    }
}
