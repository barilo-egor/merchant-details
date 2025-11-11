package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.SettleXProperties;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class SettleXOrderCreationService extends MerchantOrderCreationService<Response> {

    private final SettleXProperties settleXProperties;

    protected SettleXOrderCreationService(@Qualifier("settleXWebClient") WebClient webClient,
                                          SettleXProperties settleXProperties) {
        super(webClient, Response.class);
        this.settleXProperties = settleXProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/merchant/transactions/in").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("x-merchant-api-key", settleXProperties.key());
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setAmount(detailsRequest.getAmount());
        request.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(response.getRequisites().getBankName() + " " + response.getRequisites().getCardNumber());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantCustomId(response.getOrderId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X;
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.Conflict conflict) {
                try {
                    JsonNode response = objectMapper.readTree(conflict.getResponseBodyAsString());
                    return response.has("error")
                            && response.get("error").asText().equals("NO_REQUISITE");
                } catch (JsonProcessingException ex) {
                    return false;
                }
            }
            return false;
        };
    }
}
