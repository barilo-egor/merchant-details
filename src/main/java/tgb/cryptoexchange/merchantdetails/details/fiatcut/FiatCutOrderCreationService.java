package tgb.cryptoexchange.merchantdetails.details.fiatcut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.FiatCutProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class FiatCutOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final FiatCutProperties fiatCutProperties;

    private final CallbackConfig callbackConfig;

    protected FiatCutOrderCreationService(@Qualifier("fiatCutWebClient") WebClient webClient,
                                          FiatCutProperties fiatCutProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.fiatCutProperties = fiatCutProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return httpHeaders -> {
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("Access-Token", fiatCutProperties.token());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setAmount(request.getAmount());
        requestBody.setMethod(parseMethod(request.getMethod(), Method.class));
        requestBody.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        requestBody.setExternalId(UUID.randomUUID().toString());
        requestBody.setMerchantId(fiatCutProperties.merchantId());
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getData().getOrderId());
        detailsResponse.setBank(response.getData().getBankName());
        detailsResponse.setDetails(response.getData().getPaymentDetail().getDetail());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.BadRequest badRequest) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(badRequest.getResponseBodyAsString());
                } catch (JsonProcessingException exc) {
                    return false;
                }
                return response.has("message")
                        && "Подходящие платежные реквизиты не найдены во всех провайдерах.".equals(
                        response.get("message").asText());
            }
            return false;
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.FIAT_CUT;
    }

}
