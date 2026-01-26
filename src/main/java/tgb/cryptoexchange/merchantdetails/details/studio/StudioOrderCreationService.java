package tgb.cryptoexchange.merchantdetails.details.studio;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.StudioProperties;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class StudioOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final StudioProperties studioProperties;

    private final CallbackConfig callbackConfig;

    private final SleepingService sleepingService;

    protected StudioOrderCreationService(@Qualifier("studioWebClient") WebClient webClient,
            StudioProperties studioProperties, CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(webClient, Response.class, Callback.class);
        this.studioProperties = studioProperties;
        this.callbackConfig = callbackConfig;
        this.sleepingService = sleepingService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/orders").build();
    }

    private void addHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-API-Key", studioProperties.key());
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
        request.setMainMethod(method.name());
        request.setClientOrderId(detailsRequest.getRequestId());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant="
                + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret());
        return request;
    }

    @Override
    protected Optional<String> makeRequest(DetailsRequest detailsRequest, String body) {
        Optional<String> createOrderResponse = super.makeRequest(detailsRequest, body);
        if (createOrderResponse.isEmpty()) {
            log.debug("Отсутствует тело ответа при создании ордера мерчанта {}.", getMerchant().name());
            return Optional.empty();
        }
        String createOrderResponseBody = createOrderResponse.get();
        Response response;
        try {
            response = objectMapper.readValue(createOrderResponseBody, Response.class);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа при создании ордера мерчанта {}, body: {}",
                    currentTime, getMerchant().name(), createOrderResponseBody);
            throw new ServiceUnavailableException("Error occurred while mapping create order response: " + currentTime);
        }
        if (!Status.AWAITING_REQUISITES.name().equalsIgnoreCase(response.getStatus())) {
            return createOrderResponse;
        }
        try {
            sleepingService.sleep(8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при ожидании: {}", currentTime, e.getMessage(), e);
            throw new ServiceUnavailableException("Error occurred while wait: " + currentTime);
        }
        return Optional.ofNullable(requestService.request(
                webClient,
                HttpMethod.GET,
                uriBuilder -> uriBuilder.path("/api/v1/orders/" + response.getInternalId()).build(),
                this.headers(detailsRequest, body),
                null
        ));
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setAmount(Double.valueOf(response.getAmount()).intValue());
        detailsResponse.setMerchantOrderId(response.getInternalId());
        detailsResponse.setRequestId(response.getClientOrderId());
        detailsResponse.setMerchantOrderStatus(response.getStatus());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(response.getStatus());
        Response.Requisites requisites = response.getRequisites();
        if (requisites != null) {
            detailsResponse.setDetails(requisites.getBankName() + " " + requisites.getBik());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STUDIO;
    }

}
