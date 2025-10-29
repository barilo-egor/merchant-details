package tgb.cryptoexchange.merchantdetails.details.daopayments;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.DaoPaymentsProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class DaoPaymentsOrderCreationService extends MerchantOrderCreationService<Response> {

    private final DaoPaymentsProperties daoPaymentsProperties;

    protected DaoPaymentsOrderCreationService(@Qualifier("daoPaymentsWebClient") WebClient webClient,
                                              DaoPaymentsProperties daoPaymentsProperties) {
        super(webClient, Response.class);
        this.daoPaymentsProperties = daoPaymentsProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DAO_PAYMENTS;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder() {
        return uriBuilder -> uriBuilder.path("/api/v1/deposit").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-KEY", daoPaymentsProperties.key());
        };
    }

    @Override
    protected Request body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setMerchantOrderId(UUID.randomUUID().toString());
        request.setRequisiteType(parseMethod(requisiteRequest.getMethod(), Method.class));
        request.setAmount(requisiteRequest.getAmount().toString());
        request.setSuccessUrl(requisiteRequest.getCallbackUrl());
        request.setFailUrl(requisiteRequest.getCallbackUrl());
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        Response.TransferDetails transferDetails = response.getTransferDetails();
        if (Objects.isNull(transferDetails) || Objects.isNull(transferDetails.getBankName()) || Objects.isNull(transferDetails.getCardNumber())) {
            return Optional.empty();
        }
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setMerchant(getMerchant());
        requisiteResponse.setMerchantOrderStatus(response.getStatus().toString());
        requisiteResponse.setMerchantOrderId(response.getTransactionId());
        requisiteResponse.setRequisite(response.getTransferDetails().getBankName() + " " + response.getTransferDetails().getCardNumber());
        requisiteResponse.setAmount(new BigDecimal(response.getAmount()).intValue());
        return Optional.of(requisiteResponse);
    }
}
