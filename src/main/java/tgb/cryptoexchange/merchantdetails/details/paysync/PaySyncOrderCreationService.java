package tgb.cryptoexchange.merchantdetails.details.paysync;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PaySyncProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PaySyncOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final PaySyncProperties paySyncProperties;

    protected PaySyncOrderCreationService(@Qualifier("paySyncWebClient") WebClient webClient,
                                          PaySyncProperties paySyncProperties, ObjectMapper objectMapper) {
        super(webClient, Response.class, Callback.class);
        this.paySyncProperties = paySyncProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> {
            URI uri = uriBuilder
                    .path("/api/client{clientNumber}/amount{amount}/currency{currency}")
                    .build(
                            paySyncProperties.clientId(),
                            detailsRequest.getAmount(),
                            "RUB"
                    );
            log.info("Отправлен запрос: {} мерчанта {}", uri, getMerchant());
            return uri;
        };
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        return null;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setDetails(response.getBank() + " " + response.getCardNumber());

        return Optional.of(detailsResponse);
    }

    @Override
    protected HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSYNC;
    }
}
