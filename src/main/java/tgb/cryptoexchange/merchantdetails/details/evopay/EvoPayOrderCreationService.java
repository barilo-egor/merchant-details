package tgb.cryptoexchange.merchantdetails.details.evopay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
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
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        return uriBuilder -> uriBuilder.path("/v1/api/order/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> httpHeaders.add("x-api-key", getKey(requisiteRequest.getAmount()));
    }

    private String getKey(Integer amount) {
        if (amount.compareTo(1000) > 0) {
            return evoPayProperties.key();
        } else {
            return evoPayProperties.changeKey();
        }
    }

    @Override
    protected Request body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setCustomId(UUID.randomUUID().toString());
        request.setFiatSum(requisiteRequest.getAmount());
        request.setPaymentMethod(parseMethod(requisiteRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getRequisites())
                || Objects.isNull(response.getRequisites().getRecipientBank())
                || (Objects.isNull(response.getRequisites().getRecipientPhoneNumber())
                && Objects.isNull(response.getRequisites().getRecipientCardNumber()))) {
            return Optional.empty();
        }
        RequisiteResponse requisiteResponse = getRequisiteResponse(response);
        return Optional.of(requisiteResponse);
    }

    private RequisiteResponse getRequisiteResponse(Response response) {
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setMerchant(Merchant.EVO_PAY);
        requisiteResponse.setRequisite(Method.BANK_CARD.equals(response.getMethod())
                ? response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientCardNumber()
                : response.getRequisites().getRecipientBank() + " " + response.getRequisites().getRecipientPhoneNumber()
        );
        requisiteResponse.setMerchantOrderId(response.getId());
        requisiteResponse.setMerchantOrderStatus(response.getOrderStatus().name());
        return requisiteResponse;
    }
}
