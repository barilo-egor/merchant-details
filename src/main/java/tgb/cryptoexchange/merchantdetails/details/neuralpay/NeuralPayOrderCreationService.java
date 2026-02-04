package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.properties.NeuralPayProperties;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class NeuralPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final NeuralPayProperties neuralPayProperties;

    public static String DETAIL = "detail";

    protected NeuralPayOrderCreationService(@Qualifier("neuralPayWebClient") WebClient webClient,
                                            NeuralPayProperties neuralPayProperties) {
        super(webClient, Response.class, Callback.class);
        this.neuralPayProperties = neuralPayProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/v1/core/transactions/charge").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("accept", "application/json");
        httpHeaders.add("Authorization", "Bearer " + neuralPayProperties.token());
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest, Method.class);
        request.setMethod(Collections.singletonList(method.name()));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setAmount(Double.valueOf(response.getAmount()).intValue());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        Response.ResponseRequisite requisite = response.getRequisite();
        detailsResponse.setDetails(requisite.getBankName() + " " + requisite.getRequisite());
        return Optional.of(detailsResponse);
    }


    @Override
    protected void makeCancelRequest(CancelOrderRequest cancelOrderRequest) {
        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(Map.of(
                    "transaction_id", cancelOrderRequest.getOrderId()
            ));
        } catch (JsonProcessingException e) {
            throw new BodyMappingException("Ошибка при преобразовании тела для отмены ордера.", e);
        }
        requestService.request(webClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/v1/core/transactions/cancel").build(),
                this::addHeaders, bodyJson
        );
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            try {
                if (e instanceof WebClientResponseException.BadRequest ex) {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return response.has(DETAIL)
                            && isNoDetailsMessage(response.get(DETAIL).asText());
                }
            } catch (JsonProcessingException jsonProcessingException) {
                return false;
            }
            return false;
        };
    }

    private boolean isNoDetailsMessage(String message) {
        return message.equals("400: Payment details not found");
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NEURAL_PAY;
    }

}
