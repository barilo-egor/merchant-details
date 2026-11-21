package net.rcetech.md.integrations.platform.paysync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PaySyncProperties;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PaySyncOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final PaySyncProperties paySyncProperties;

    protected PaySyncOrderCreationService(@Qualifier("paySyncRestClient") RestClient restClient,
                                          PaySyncProperties paySyncProperties, ObjectMapper objectMapper) {
        super(restClient, Response.class, Callback.class);
        this.paySyncProperties = paySyncProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
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
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        return null;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setBank(response.getBank());
        detailsResponse.setDetails(response.getCardNumber());

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
