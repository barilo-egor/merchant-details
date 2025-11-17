package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallbackMock;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.LevelPayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class LevelPayOrderCreationService extends MerchantOrderCreationService<Response, MerchantCallbackMock> {

    private final LevelPayProperties levelPayProperties;

    protected LevelPayOrderCreationService(WebClient webClient, LevelPayProperties levelPayProperties) {
        super(webClient, Response.class, MerchantCallbackMock.class);
        this.levelPayProperties = levelPayProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("Access-Token", levelPayProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setMerchantId(levelPayProperties.merchantId());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setPaymentDetailType(method);
        if (Method.ALFA_ALFA.equals(method)) {
            request.setPaymentGateway("alfa-alfa");
        }
        request.setExternalId(UUID.randomUUID().toString());
        request.setCallbackUrl(detailsRequest.getCallbackUrl());
        request.setFloatingAmount(true);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        if (Objects.nonNull(response.getData().getPaymentGatewayName())) {
            detailsResponse.setDetails(response.getData().getPaymentGatewayName() + " " + response.getData().getPaymentDetail().getDetail());
        } else {
            detailsResponse.setDetails(response.getData().getPaymentGateway() + " " + response.getData().getPaymentDetail().getDetail());
        }
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getOrderId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        detailsResponse.setAmount(Integer.parseInt(response.getData().getAmount()));
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.BadRequest ex) {
                JsonNode response;
                try {
                    response = objectMapper.readTree(ex.getResponseBodyAsString());
                } catch (JsonProcessingException exc) {
                    return false;
                }
                return response.has("success") && !response.get("success").asBoolean();
            }
            return false;
        };
    }
}
