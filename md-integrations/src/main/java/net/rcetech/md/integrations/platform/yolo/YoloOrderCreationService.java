package net.rcetech.md.integrations.platform.yolo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.integrations.properties.YoloProperties;
import tools.jackson.core.JacksonException;

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

    protected YoloOrderCreationService(@Qualifier("yoloRestClient") RestClient restClient,
                                       YoloProperties yoloProperties, CallbackConfig callbackConfig) {
        super(restClient, Response.class, Callback.class);
        this.yoloProperties = yoloProperties;
        this.callbackConfig = callbackConfig;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/client/orders/deposit")
                .queryParam("accountId", yoloProperties.accountId()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        if (jwtData.getAccessToken() == null || Instant.now().plus(5, ChronoUnit.MINUTES).isAfter(jwtData.getExpiresAt())) {
            Optional<String> jwtResponse = Optional.ofNullable(requestService.request(
                    restClient, method(), uriBuilder -> uriBuilder.path("/api/client/auth/login").build(),
                    h -> h.add("Content-Type", "application/json"), mapBody()));
            if (jwtResponse.isEmpty()) {
                log.debug("Yolo jwt ответ пуст!");
                throw new ServiceUnavailableException("Error jwt response is empty: " + System.currentTimeMillis());
            }
            String responseBody = jwtResponse.get();
            JwtData response;
            try {
                response = objectMapper.readValue(responseBody, JwtData.class);
            } catch (JacksonException e) {
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
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setExternalId(UUID.randomUUID().toString());
        request.setValue(String.valueOf(detailsRequest.getAmount()));
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setUseFastPayment(Method.SBP.equals(method));
        request.setWebhookUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setBank(response.getBankName());
        if (Objects.nonNull(response.getContactNumber())) {
            detailsResponse.setDetails(response.getContactNumber());
        } else {
            detailsResponse.setDetails(response.getAccountNumber());
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
        } catch (JacksonException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при маппинге тела запроса(credentials = {}): {}", currentTime, yoloProperties.credentials(), e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while mapping body: " + currentTime + ".", e);
        }
    }

}
