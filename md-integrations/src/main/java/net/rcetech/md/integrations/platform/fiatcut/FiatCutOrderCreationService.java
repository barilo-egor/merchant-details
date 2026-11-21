package net.rcetech.md.integrations.platform.fiatcut;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.FiatCutProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

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

    protected FiatCutOrderCreationService(@Qualifier("fiatCutRestClient") RestClient restClient,
                                          FiatCutProperties fiatCutProperties, CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.fiatCutProperties = fiatCutProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("Access-Token", fiatCutProperties.token());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        request.setExternalId(UUID.randomUUID().toString());
        request.setMerchantId(fiatCutProperties.merchantId());
        return request;
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
            if (e instanceof HttpClientErrorException.BadRequest badRequest) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(badRequest.getResponseBodyAsString());
                } catch (JacksonException exc) {
                    return false;
                }
                return response.has("message")
                        && "Подходящие платежные реквизиты не найдены во всех провайдерах.".equals(
                        response.get("message").asString());
            }
            return false;
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.FIAT_CUT;
    }

}
