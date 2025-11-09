package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HoneyMoneyProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Slf4j
public class HoneyMoneyOrderCreationService extends MerchantOrderCreationService<Response> {

    private final HoneyMoneyProperties honeyMoneyProperties;

    private final SignatureService signatureService;

    protected HoneyMoneyOrderCreationService(@Qualifier("honeyMoneyWebClient") WebClient webClient,
                                             HoneyMoneyProperties honeyMoneyProperties, SignatureService signatureService) {
        super(webClient, Response.class);
        this.honeyMoneyProperties = honeyMoneyProperties;
        this.signatureService = signatureService;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HONEY_MONEY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path(method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + honeyMoneyProperties.authToken());
            httpHeaders.add("Content-Type", "application/json");
            Method method = parseMethod(detailsRequest.getMethod(), Method.class);
            httpHeaders.add("X-Signature", signatureService.hmacSHA256(body, URI.create(honeyMoneyProperties.url() + method.getUri()), honeyMoneyProperties.signToken()));
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setExtId(UUID.randomUUID().toString());
        request.setBank(parseMethod(detailsRequest.getMethod(), Method.class).getBank());
        request.setCallbackUrl(detailsRequest.getCallbackUrl());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        String requisite = Objects.nonNull(response.getPhoneNumber()) ? response.getPhoneNumber() : response.getCardNumber();
        detailsResponse.setDetails(response.getBankName() + " " + requisite);
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        detailsResponse.setMerchantOrderStatus(Status.PENDING.name());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof WebClientResponseException.BadRequest ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isNoDetails(response) || isAmountError(response);
                } catch (JsonProcessingException jsonProcessingException) {
                    return false;
                }
            }
            return false;
        };
    }

    private boolean isAmountError(JsonNode response) {
        if (response.has("errors")) {
            JsonNode errors = response.get("errors");
            if (errors.has("Amount")) {
                JsonNode amount = errors.get("Amount");
                if (amount.isArray() && amount.size() == 1) {
                    return amount.get(0).asText().startsWith("Amount must be");
                }
            }
        }
        return false;
    }

    private boolean isNoDetails(JsonNode response) {
        return response.has("detail")
                && response.get("detail").asText().equals("No requisites available for the moment. Please try again later.");
    }
}
