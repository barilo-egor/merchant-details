package tgb.cryptoexchange.merchantdetails.details.daopayments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import tgb.cryptoexchange.merchantdetails.properties.DaoPaymentsProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class DaoPaymentsOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final DaoPaymentsProperties daoPaymentsProperties;

    protected DaoPaymentsOrderCreationService(@Qualifier("daoPaymentsWebClient") WebClient webClient,
                                              DaoPaymentsProperties daoPaymentsProperties) {
        super(webClient, Response.class, Callback.class);
        this.daoPaymentsProperties = daoPaymentsProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DAO_PAYMENTS;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/deposit").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-KEY", daoPaymentsProperties.key());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setMerchantOrderId(UUID.randomUUID().toString());
        request.setRequisiteType(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setAmount(detailsRequest.getAmount().toString());
        request.setSuccessUrl(detailsRequest.getCallbackUrl());
        request.setFailUrl(detailsRequest.getCallbackUrl());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.TransferDetails transferDetails = response.getTransferDetails();
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(response.getStatus().toString());
        detailsResponse.setMerchantOrderId(response.getTransactionId());
        detailsResponse.setDetails(transferDetails.getBankName() + " " + transferDetails.getCardNumber());
        detailsResponse.setAmount(new BigDecimal(response.getAmount()).intValue());
        return Optional.of(detailsResponse);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            try {
                if (e instanceof WebClientResponseException.InternalServerError ex) {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return response.has("error")
                            && response.get("error").asText().startsWith("deposit processing failed: all traders failed");
                }
            } catch (JsonProcessingException jsonProcessingException) {
                return false;
            }
            return false;
        };
    }
}
