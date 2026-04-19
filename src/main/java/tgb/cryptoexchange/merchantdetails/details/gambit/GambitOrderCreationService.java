package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.GambitProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class GambitOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final GambitProperties gambitProperties;

    protected GambitOrderCreationService(WebClient webClient, GambitProperties gambitProperties) {
        super(webClient, Response.class, Callback.class);
        this.gambitProperties = gambitProperties;
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/orders/init").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + gambitProperties.key());
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setMethod(method);
        request.setOrderId(UUID.randomUUID().toString() + System.currentTimeMillis());
        request.setAmount(detailsRequest.getAmount());
        request.setTerminalUid(gambitProperties.terminal());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(response.getAmount().intValue());
        Response.Requisites requisites = response.getPaymentDetails();
        if (Objects.nonNull(response.getPaymentDetails().getPhone())) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getPhone());
        } else {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getCardNumber());
        }
        return Optional.of(detailsResponse);
    }
}
