package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.EmptyResponseBodyException;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.YoloProperties;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
public abstract class YoloOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final YoloProperties yoloProperties;

    protected final CallbackConfig callbackConfig;

    protected final JwtData jwtData = new JwtData();

    protected YoloOrderCreationService(@Qualifier("yoloWebClient") WebClient webClient,
                                       YoloProperties yoloProperties, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.yoloProperties = yoloProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/client/orders/deposit")
                .queryParam("accountId", yoloProperties.accountId()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        if (jwtData.getAccessToken() == null || Instant.now().plus(5, ChronoUnit.MINUTES).isAfter(jwtData.getExpiresAt())) {
            Optional<String> jwtResponse = Optional.ofNullable(requestService.request(
                    webClient, method(), uriBuilder -> uriBuilder.path("/api/client/auth/login").build(),
                    h -> h.add("Content-Type", "application/json"), mapBody()));
            if (jwtResponse.isEmpty()) {
                log.debug("Yolo jwt ответ пуст!");
                throw new EmptyResponseBodyException("Error jwt response is empty: " + System.currentTimeMillis());
            }
            String responseBody = jwtResponse.get();
            JwtData response;
            try {
                response = objectMapper.readValue(responseBody, JwtData.class);
            } catch (JsonProcessingException e) {
                long currentTime = System.currentTimeMillis();
                log.debug("{} Ошибки преобразования ответа при получении jwt токена {}, body: {}",
                        currentTime, getMerchant().name(), responseBody);
                throw new ServiceUnavailableException("Error occurred while mapping create order response: " + currentTime);
            }
            jwtData.setAccessToken(response.getAccessToken());
            jwtData.setExpiresAt(response.getExpiresAt());
        }

        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-Store-Key", yoloProperties.storeKey());
            httpHeaders.add("X-Secret-CB", callbackConfig.getCallbackSecret());
            httpHeaders.add("Authorization", "Bearer " + jwtData.getAccessToken());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setExternalId(UUID.randomUUID().toString());
        request.setValue(String.valueOf(detailsRequest.getAmount()));
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        request.setUseFastPayment(Method.SBP.equals(method));
        request.setWebhookUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        if (Objects.nonNull(response.getContactNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getContactNumber());
        } else {
            detailsResponse.setDetails(response.getBankName() + " " + response.getAccountNumber());
        }
        detailsResponse.setMerchantOrderId(response.getOrderId());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(response.getValue().intValue());
        detailsResponse.setMerchantOrderStatus(Status.CREATED.name());
        return Optional.of(detailsResponse);
    }

    private String mapBody() {
        try {
            return objectMapper.writeValueAsString(yoloProperties.credentials());
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при маппинге тела запроса(credentials = {}): {}", currentTime, yoloProperties.credentials(), e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while mapping body: " + currentTime + ".", e);
        }
    }

}
