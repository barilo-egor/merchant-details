package tgb.cryptoexchange.merchantdetails.details.onlypays;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallbackMock;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnlyPaysProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class OnlyPaysOrderCreationService extends MerchantOrderCreationService<Response, MerchantCallbackMock> {
    
    private final OnlyPaysProperties onlyPaysProperties;
    
    protected OnlyPaysOrderCreationService(@Qualifier("onlyPaysWebClient") WebClient webClient, 
                                           OnlyPaysProperties onlyPaysProperties) {
        super(webClient, Response.class, MerchantCallbackMock.class);
        this.onlyPaysProperties = onlyPaysProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONLY_PAYS;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/get_requisite").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setApiId(onlyPaysProperties.id());
        request.setAmount(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        if (Method.SIM.equals(method)) {
            request.setSim(true);
        }
        switch (method) {
            case SIM -> request.setSim(true);
            case ALFA_ALFA -> request.setBank("Альфа");
            case OZON_OZON -> request.setBank("Озон");
            default -> {/* не требует действий */}
        }
        request.setSecretKey(onlyPaysProperties.secret());
        request.setPersonalId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(response.getData().getBank() + " " + response.getData().getRequisite());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getId());
        detailsResponse.setMerchantOrderStatus(Status.WAITING.name());
        return Optional.of(detailsResponse);
    }
}
