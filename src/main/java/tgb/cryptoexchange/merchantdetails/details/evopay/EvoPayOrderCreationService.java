package tgb.cryptoexchange.merchantdetails.details.evopay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.EvoPayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class EvoPayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final EvoPayProperties evoPayProperties;

    protected EvoPayOrderCreationService(@Qualifier("evoPayWebClient") WebClient webClient,
                                         EvoPayProperties evoPayProperties) {
        super(webClient, Response.class);
        this.evoPayProperties = evoPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EVO_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/v1/api/order/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("x-api-key", getKey(detailsRequest.getAmount()));
    }

    private String getKey(Integer amount) {
        if (amount.compareTo(1000) > 0) {
            return evoPayProperties.key();
        } else {
            return evoPayProperties.changeKey();
        }
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setCustomId(UUID.randomUUID().toString());
        request.setFiatSum(detailsRequest.getAmount());
        request.setPaymentMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.EVO_PAY);
        if (Objects.nonNull(response.getRequisites().getRecipientCardNumber())) {
            detailsResponse.setDetails(
                    response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientCardNumber()
            );
        } else {
            detailsResponse.setDetails(
                    response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientPhoneNumber()
            );
        }
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getOrderStatus().name());
        return Optional.of(detailsResponse);
    }
}
