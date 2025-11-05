package tgb.cryptoexchange.merchantdetails.details.bitzone;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
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

@Service
public class BitZoneOrderCreationService extends MerchantOrderCreationService<Response> {

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
}
