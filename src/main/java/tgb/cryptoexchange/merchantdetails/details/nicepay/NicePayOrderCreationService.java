package tgb.cryptoexchange.merchantdetails.details.nicepay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.NicePayProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class NicePayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final NicePayProperties nicePayProperties;

    protected NicePayOrderCreationService(@Qualifier("nicePayWebClient") WebClient webClient,
                                          NicePayProperties nicePayProperties) {
        super(webClient, Response.class);
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
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setMerchantId(nicePayProperties.merchantId());
        request.setSecret(nicePayProperties.secret());
        request.setOrderId(UUID.randomUUID().toString());
        request.setAmount(new BigDecimal(detailsRequest.getAmount()).multiply(new BigDecimal(100)).intValue());
        Method nicePayMethod = parseMethod(detailsRequest.getMethod(), Method.class);
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
        String details = findDetails(response);
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(details);
        detailsResponse.setMerchant(Merchant.NICE_PAY);
        detailsResponse.setMerchantOrderStatus(response. getData().getStatus().name());
        detailsResponse.setMerchantOrderId(response.getData().getPaymentId());
        return Optional.of(detailsResponse);
    }

    private static String findDetails(Response response) {
        String details = null;
        if (Objects.nonNull(response.getData().getSubMethod()) && Objects.nonNull(response.getData().getSubMethod().getNames())) {
            details = response.getData().getSubMethod().getNames().getRu() + " " + response.getData().getDetails().getWallet();
        } else if (Objects.nonNull(response.getData().getDetails()) && Objects.nonNull(response.getData().getDetails().getComment())) {
            details = response.getData().getDetails().getComment() + " " + response.getData().getDetails().getWallet();
        } else if (Objects.nonNull(response.getData().getDetails())) {
            details = response.getData().getDetails().getWallet();
        }
        return details;
    }
}
