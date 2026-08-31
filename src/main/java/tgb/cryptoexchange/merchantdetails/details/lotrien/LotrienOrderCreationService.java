package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class LotrienOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final LotrienProperties lotrienProperties;

    protected LotrienOrderCreationService(WebClient webClient, LotrienProperties lotrienProperties) {
        super(webClient, Response.class, Callback.class);
        this.lotrienProperties = lotrienProperties;
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
        return uriBuilder -> uriBuilder.path("/order/payin").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("X-API-Key", lotrienProperties.key());
    }

    @Override
    protected Request body(OrderCreationRequest request) {
        Request requestBody = new Request();
        Method method = parseMethod(request.getMethod(), Method.class);
        requestBody.setPaymentMethod(method);
        requestBody.setFiatSum(String.format(Locale.US, "%.2f", request.getAmount().doubleValue()));
        return requestBody;
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
        if (List.of(Method.PDF_BANK_CARD, Method.BANK_CARD).contains(response.getPaymentMethod())) {
            detailsResponse.setDetails(requisites.getCardNumber());
        } else {
            detailsResponse.setDetails(requisites.getPhoneNumber());
        }

        return Optional.of(detailsResponse);
    }

}
