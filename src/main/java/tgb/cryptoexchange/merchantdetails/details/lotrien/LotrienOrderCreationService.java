package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.IDetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class LotrienOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final LotrienProperties lotrienProperties;

    protected LotrienOrderCreationService(@Qualifier("lotrienWebClient") WebClient lotrienWebClient,
                                          LotrienProperties lotrienProperties) {
        super(lotrienWebClient, Response.class, Callback.class);
        this.lotrienProperties = lotrienProperties;
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(IDetailsRequest detailsRequest, String merchantMethod) {
        return uriBuilder -> uriBuilder.path("/order/payin").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(IDetailsRequest detailsRequest, String merchantMethod, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("X-API-Key", lotrienProperties.key());
    }

    @Override
    protected Request body(IDetailsRequest detailsRequest, String merchantMethod) {
        Request request = new Request();
        Method method = parseMethod(merchantMethod, Method.class);
        request.setPaymentMethod(method);
        request.setFiatSum(String.format(Locale.US, "%.2f", detailsRequest.getAmount().doubleValue()));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(new BigDecimal(response.getAmount()).intValue());
        Response.Requisites requisites = response.getRequisites();
        detailsResponse.setBank(requisites.getBank());
        if (Method.BANK_CARD.equals(response.getPaymentMethod())) {
            detailsResponse.setDetails(requisites.getCardNumber());
        } else {
            detailsResponse.setDetails(requisites.getPhoneNumber());
        }
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.LOTRIEN;
    }

}
