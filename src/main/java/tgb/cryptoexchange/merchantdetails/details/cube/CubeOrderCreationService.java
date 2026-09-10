package tgb.cryptoexchange.merchantdetails.details.cube;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.CubeProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class CubeOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final CubeProperties cubeProperties;

    protected final CallbackConfig callbackConfig;

    protected CubeOrderCreationService(WebClient webClient, CubeProperties cubeProperties,
                                       CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.cubeProperties = cubeProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/transactions/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("Apipublic", cubeProperties.key());
        headers.add("Apiprivate", cubeProperties.privateKey());
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setMethod(method);
        request.setExternalId(UUID.randomUUID().toString());
        setCallback(request);
        return request;
    }

    protected void setCallback(Request request) {
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback/" + getMerchant() + "?transactionId="
                + request.getExternalId() + "&secret=" + callbackConfig.getCallbackSecret());
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getInternalId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        detailsResponse.setDetails(response.getData().getBankName() + " " + response.getData().getReceiver());
        detailsResponse.setAmount(response.getData().getAmount().intValue());

        return Optional.of(detailsResponse);
    }

    @Override
    protected Optional<String> makeFetchCallbackData(String transactionId) {
        String response = requestService.request(webClient, HttpMethod.GET,
                uriBuilder(null),
                headers(null, null),
                null
        );
        try {
            Response.Data data = objectMapper.readValue(response, Response.Data.class);

            Callback callback = new Callback();
            callback.setId(data.getInternalId());
            callback.setStatus(data.getStatus());
            String callbackData = objectMapper.writeValueAsString(callback);
            return Optional.of(callbackData);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа информации о транзакции мерчанта {}, body: {}", currentTime,
                    getMerchant().name(), response);
            throw new ServiceUnavailableException("Error occurred while mapping get callback data response: " + currentTime);
        }
    }

}

