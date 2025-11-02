package tgb.cryptoexchange.merchantdetails.details.pspware;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PspWareProperties;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class PspWareOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PspWareProperties pspWareProperties;
    
    protected PspWareOrderCreationService(@Qualifier("pspWareWebClient") WebClient webClient, 
                                          PspWareProperties pspWareProperties) {
        super(webClient, Response.class);
        this.pspWareProperties = pspWareProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PSP_WARE;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/merchant/v2/orders").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("X-API-KEY", pspWareProperties.token());
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setSum(detailsRequest.getAmount());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setPayTypes(List.of());
        if (Method.TRANSGRAN_PHONE.equals(method)) {
            request.setGeos(List.of("TJK"));
        } else if (Method.SBP.equals(method)) {
            request.setGeos(List.of("RU", "ABH"));
        } else {
            request.setGeos(List.of("RU"));
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getCard()) || Objects.isNull(response.getBankName())) {
            return Optional.empty();
        }
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setDetails(response.getBankName() + " " + response.getCard());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        return Optional.of(detailsResponse);
    }
}
