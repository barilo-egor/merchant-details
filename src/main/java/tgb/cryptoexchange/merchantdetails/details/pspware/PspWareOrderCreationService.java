package tgb.cryptoexchange.merchantdetails.details.pspware;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.details.VoidCallback;
import tgb.cryptoexchange.merchantdetails.properties.PspWareProperties;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Взаимодействие с данным мерчантом приостановлено.
 * Отсутствует реализация обновления статусов ордеров.
 */
public class PspWareOrderCreationService extends MerchantOrderCreationService<Response, VoidCallback> {

    private final PspWareProperties pspWareProperties;
    
    protected PspWareOrderCreationService(@Qualifier("pspWareWebClient") WebClient webClient, 
                                          PspWareProperties pspWareProperties) {
        super(webClient, Response.class, VoidCallback.class);
        this.pspWareProperties = pspWareProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PSP_WARE;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/merchant/v2/orders").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return httpHeaders -> httpHeaders.add("X-API-KEY", pspWareProperties.token());
    }

    @Override
    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        requestBody.setSum(request.getAmount());
        Method method = parseMethod(request.getMethod(), Method.class);
        requestBody.setPayTypes(List.of(method));
        if (Method.TRANSGRAN_PHONE.equals(method)) {
            requestBody.setGeos(List.of("TJK"));
        } else if (Method.SBP.equals(method)) {
            requestBody.setGeos(List.of("RU", "ABH"));
        } else {
            requestBody.setGeos(List.of("RU"));
        }
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setBank(response.getBankName());
        detailsResponse.setDetails(response.getCard());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        return Optional.of(detailsResponse);
    }
}
