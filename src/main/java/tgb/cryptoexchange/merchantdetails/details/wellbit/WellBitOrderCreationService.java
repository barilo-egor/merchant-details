package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.VoidCallback;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.WellBitProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Взаимодействие с данным мерчантом приостановлено.
 * Отсутствует реализация обновления статусов ордеров.
 */
@Slf4j
public class WellBitOrderCreationService extends MerchantOrderCreationService<Response, VoidCallback> {

    private final WellBitProperties wellBitProperties;

    private final SignatureService signatureService;

    private String hashSecret;

    protected WellBitOrderCreationService(@Qualifier("wellBitWebClient") WebClient webClient,
                                          WellBitProperties wellBitProperties, SignatureService signatureService) {
        super(webClient, Response.class, VoidCallback.class);
        this.wellBitProperties = wellBitProperties;
        this.signatureService = signatureService;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.WELL_BIT;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/payment/make").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("token", wellBitProperties.token());
            httpHeaders.add("Content-Type", "application/json");
            try {
                httpHeaders.add("secret", getHashSecret());
            } catch (NoSuchAlgorithmException e) {
                log.error("Ошибка формирования подписи мерчанта {}. detailsRequest={}, body={}", getMerchant().name(), detailsRequest, body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    private String getHashSecret() throws NoSuchAlgorithmException {
        if (hashSecret == null) {
            hashSecret = signatureService.getMD5Hash(
                    wellBitProperties.secret() + wellBitProperties.login() + wellBitProperties.id()
            );
        }
        return hashSecret;
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request createOrderRequest = new Request();
        createOrderRequest.setCredentialRequire("yes");
        createOrderRequest.setAmount(detailsRequest.getAmount());
        createOrderRequest.setCredentialType(parseMethod(detailsRequest, Method.class).getValue());
        createOrderRequest.setCustomNumber(UUID.randomUUID().toString());
        return createOrderRequest;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setDetails(response.getPayment().getCredentialAdditionalBank() + " " + response.getPayment().getCredential());
        detailsResponse.setMerchantOrderStatus(response.getPayment().getStatus().name());
        detailsResponse.setMerchantOrderId(response.getPayment().getId().toString());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<String> hasResponseNoDetailsErrorPredicate() {
        return rawResponse -> {
            JsonNode response;
            try {
                response = objectMapper.readTree(rawResponse);
            } catch (JsonProcessingException e) {
                long currentTime = System.currentTimeMillis();
                log.error("{} Ошибка маппинга ответа мерчанта {}, оригинальный ответ= {}, ошибка: {}",
                        currentTime, getMerchant().name(), rawResponse, e.getMessage(), e
                );
                throw new ServiceUnavailableException("Error occurred while mapping merchant response: " + currentTime + ".", e);
            }
            return response.isArray() && !response.isEmpty() && response.get(0).has("code")
                    && response.get(0).get("code").asText().equals("E0010");
        };
    }
}
