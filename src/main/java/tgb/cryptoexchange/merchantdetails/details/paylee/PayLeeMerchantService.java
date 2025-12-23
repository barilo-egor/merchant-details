package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.hashids.Hashids;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PayLeeProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class PayLeeMerchantService extends MerchantOrderCreationService<Response, Callback> {

    static final String NON_FIELD_ERRORS = "nonFieldErrors";

    private final PayLeeProperties payLeeProperties;

    private final Hashids hashids;

    protected PayLeeMerchantService(WebClient webClient, PayLeeProperties payLeeProperties) {
        super(webClient, Response.class, Callback.class);
        this.payLeeProperties = payLeeProperties;
        this.hashids = new Hashids(payLeeProperties.clientIdSalt(), 8);
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/partners/purchases/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Token " + payLeeProperties.token());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setPrice(detailsRequest.getAmount());
        request.setRequisitesType(parseMethod(detailsRequest, Method.class));
        if (Objects.nonNull(detailsRequest.getChatId())) {
            request.setClientId(hashids.encode(detailsRequest.getChatId()));
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse vo = new DetailsResponse();
        vo.setDetails(response.getBankName() + " " + response.getRequisites());
        vo.setMerchant(getMerchant());
        vo.setMerchantOrderId(response.getId().toString());
        vo.setMerchantOrderStatus(response.getStatus().name());
        vo.setAmount(response.getPrice().intValue());
        return Optional.of(vo);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.BadRequest ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isNoTrader(response) || isAmountError(response);
                } catch (JsonProcessingException jsonProcessingException) {
                    return false;
                }
            }
            return false;
        };
    }

    private boolean isAmountError(JsonNode response) {
        if (response.has("price")) {
            JsonNode price = response.get("price");
            return price.isArray() && price.size() == 1 && price.get(0).asText().startsWith("Убедитесь, что это значение");
        }
        return false;
    }

    private boolean isNoTrader(JsonNode response) {
        return response.has(NON_FIELD_ERRORS)
                && response.get(NON_FIELD_ERRORS).isArray()
                && response.get(NON_FIELD_ERRORS).size() == 1
                && response.get(NON_FIELD_ERRORS).get(0).asText()
                .equals("Нет доступного трейдера для вашего запроса. Попробуйте повторить позже.");
    }
}
