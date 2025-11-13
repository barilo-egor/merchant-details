package tgb.cryptoexchange.merchantdetails.details.bitzone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.BitZoneProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Slf4j
public class BitZoneOrderCreationService extends MerchantOrderCreationService<Response> {

    private static final String MESSAGE = "message";

    private final BitZoneProperties bitZoneProperties;
    
    protected BitZoneOrderCreationService(@Qualifier("bitZoneWebClient") WebClient webClient,
                                          BitZoneProperties bitZoneProperties) {
        super(webClient, Response.class);
        this.bitZoneProperties = bitZoneProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BIT_ZONE;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/payment/trading/pay-in").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("x-api-key", bitZoneProperties.key());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setFiatAmount(detailsRequest.getAmount());
        request.setMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setExtra(new Request.Extra(UUID.randomUUID().toString()));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        String requisite;
        if (Method.SBP.equals(response.getMethod())) {
            requisite = response.getRequisite().getBank() + " " + response.getRequisite().getSbpNumber();
        } else {
            requisite = response.getRequisite().getBank() + " " + response.getRequisite().getRequisites();
        }
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderStatus(response.getStatus().name());
        requisiteVO.setMerchantOrderId(response.getId());
        requisiteVO.setDetails(requisite);
        return Optional.of(requisiteVO);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            try {
                if (e instanceof WebClientResponseException.Forbidden ex) {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return response.has(MESSAGE)
                            && (response.get(MESSAGE).asText().equals("SBP_METHOD_DISABLED_PLEASE_CONTACT_SUPPORT")
                            || response.get(MESSAGE).asText().equals("CANT_CREATE_TRADE_FOR_THIS_AMOUNT"));
                } else if (e instanceof WebClientResponseException.GatewayTimeout) {
                    return true;
                }
            } catch (JsonProcessingException jsonProcessingException) {
                return false;
            }
            return false;
        };
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
            return response.has(MESSAGE)
                    && response.get(MESSAGE).asText().equals("SBP_METHOD_DISABLED_PLEASE_CONTACT_SUPPORT");
        };
    }
}
