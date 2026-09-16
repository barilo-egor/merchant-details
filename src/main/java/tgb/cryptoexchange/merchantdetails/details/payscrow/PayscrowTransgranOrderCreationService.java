package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowProperties;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowTransgranProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PayscrowTransgranOrderCreationService extends MerchantOrderCreationService<ResponseTransgran, Callback> {

    protected final String baseUrl;
    private final PayscrowProperties payscrowProperties;
    private final CallbackConfig callbackConfig;

    protected PayscrowTransgranOrderCreationService(@Qualifier("payscrowTransgranWebClient") WebClient webClient,
                                                    PayscrowTransgranProperties payscrowTransgranProperties,
                                                    @Value("${gateway-url}") String baseUrl, CallbackConfig callbackConfig) {
        super(webClient, ResponseTransgran.class, Callback.class);
        this.payscrowProperties = payscrowTransgranProperties;
        this.callbackConfig = callbackConfig;
        this.baseUrl = baseUrl;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/form/create").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-Key", payscrowProperties.key());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        Request.OrderData orderData = new Request.OrderData();
        orderData.setAmount(detailsRequest.getAmount());
        orderData.setClientOrderId(UUID.randomUUID().toString());
        request.setOrderData(orderData);

        String redirectUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/merchant-details/callback/redirect")
                .queryParam("secret", callbackConfig.getCallbackSecret())
                .queryParam("merchant", getMerchant().name())
                .queryParam("bot_url", detailsRequest.getRedirectUrl())
                .queryParam("transaction_id", orderData.getClientOrderId())
                .toUriString();
        request.setRedirectUrl(redirectUrl);
        request.setReturnUrl(redirectUrl);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(ResponseTransgran response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        ResponseTransgran.Data data = response.getData();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(data.getOrderData().getOrderId());
        detailsResponse.setDetails(data.getFormUrl());
        detailsResponse.setAmount(data.getOrderData().getAmount().intValue());

        return Optional.of(detailsResponse);
    }

    @Override
    protected Optional<String> makeFetchCallbackData(String transactionId) {
        String cancelUrl = "/api/v1/order/search-by-client-order-id";
        String response = requestService.request(webClient, HttpMethod.GET,
                uriBuilder -> uriBuilder.path(cancelUrl)
                        .queryParam("id", transactionId).build(),
                headers(null, null),
                null
        );
        try {
            TransgranTxDTO txData = objectMapper.readValue(response, TransgranTxDTO.class);
            TransgranTxDTO.Order.Item data = txData.getOrder().getItems().getFirst();

            if (!txData.hasDetails() || !txData.validate().isValid()) {
                long currentTime = System.currentTimeMillis();
                log.debug("{} Пустой ответ при получении информации о транзакции мерчанта {}, body: {}", currentTime,
                        getMerchant().name(), response);
            }

            Callback callback = new Callback();
            Callback.Payload payload = new Callback.Payload();
            payload.setId(data.getOrderId());
            payload.setStatus(data.getStatus());
            callback.setPayload(payload);
            String callbackData = objectMapper.writeValueAsString(callback);
            return Optional.of(callbackData);
        } catch (JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.debug("{} Ошибки преобразования ответа информации о транзакции мерчанта {}, body: {}", currentTime,
                    getMerchant().name(), response);
            throw new ServiceUnavailableException("Error occurred while mapping get callback data response: " + currentTime);
        }
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_TRANSGRAN;
    }
}
