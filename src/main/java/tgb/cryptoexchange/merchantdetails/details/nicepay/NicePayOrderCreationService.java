package tgb.cryptoexchange.merchantdetails.details.nicepay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.VoidCallback;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.NicePayProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Взаимодействие с данным мерчантом приостановлено.
 * Отсутствует реализация обновления статусов ордеров, подтверждения и отправки чеков.
 */
@Slf4j
public class NicePayOrderCreationService extends MerchantOrderCreationService<Response, VoidCallback> {

    private final NicePayProperties nicePayProperties;

    protected NicePayOrderCreationService(@Qualifier("nicePayWebClient") WebClient webClient,
                                          NicePayProperties nicePayProperties) {
        super(webClient, Response.class, VoidCallback.class);
        this.nicePayProperties = nicePayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NICE_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/h2hOneRequestPayment").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setMerchantId(nicePayProperties.merchantId());
        request.setSecret(nicePayProperties.secret());
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(new BigDecimal(detailsRequest.getAmount()).multiply(new BigDecimal(100)).intValue());
        Method nicePayMethod = parseMethod(detailsRequest, Method.class);
        request.setMethod(nicePayMethod);
        if (Method.SBP_RU.equals(nicePayMethod)) {
            request.setMethodSBP("onlyRU");
        } else if (Method.SBP_TRANSGRAN.equals(nicePayMethod)) {
            request.setMethodSBP("onlyINT");
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.Data data = response.getData();
        String details = findDetails(data);
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(details);
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderStatus(data.getStatus().name());
        detailsResponse.setMerchantOrderId(data.getPaymentId());
        return Optional.of(detailsResponse);
    }

    private String findDetails(Response.Data data) {
        String details;
        if (Objects.nonNull(data.getSubMethod())) {
            details = data.getSubMethod().getNames().getRu() + " " + data.getDetails().getWallet();
        } else if (Objects.nonNull(data.getDetails().getComment())) {
            details = data.getDetails().getComment() + " " + data.getDetails().getWallet();
        } else {
            details = data.getDetails().getWallet();
        }
        return details;
    }
}
