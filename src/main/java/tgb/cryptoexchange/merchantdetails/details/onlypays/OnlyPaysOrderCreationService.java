package tgb.cryptoexchange.merchantdetails.details.onlypays;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.OnlyPaysProperties;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class OnlyPaysOrderCreationService extends MerchantOrderCreationService<Response, Callback> {
    
    private final OnlyPaysProperties onlyPaysProperties;
    
    protected OnlyPaysOrderCreationService(@Qualifier("onlyPaysWebClient") WebClient webClient, 
                                           OnlyPaysProperties onlyPaysProperties) {
        super(webClient, Response.class, Callback.class);
        this.onlyPaysProperties = onlyPaysProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONLY_PAYS;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/get_requisite").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setApiId(onlyPaysProperties.id());
        requestBody.setAmount(request.getAmount());
        Method method = parseMethod(request.getMethod(), Method.class);
        requestBody.setMethod(method);
        if (Method.SIM.equals(method)) {
            requestBody.setSim(true);
        }
        switch (method) {
            case SIM -> requestBody.setSim(true);
            case ALFA_ALFA -> requestBody.setBank("Альфа");
            case OZON_OZON -> requestBody.setBank("Озон");
            default -> {/* не требует действий */}
        }
        requestBody.setSecretKey(onlyPaysProperties.secret());
        requestBody.setPersonalId(UUID.randomUUID().toString());
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setBank(response.getData().getBank());
        detailsResponse.setDetails(response.getData().getRequisite());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getId());
        detailsResponse.setMerchantOrderStatus(Status.WAITING.name());
        return Optional.of(detailsResponse);
    }
}
