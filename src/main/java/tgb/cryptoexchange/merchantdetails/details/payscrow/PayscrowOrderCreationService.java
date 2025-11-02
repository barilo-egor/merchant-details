package tgb.cryptoexchange.merchantdetails.details.payscrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class PayscrowOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PayscrowProperties payscrowPropertiesImpl;

    protected PayscrowOrderCreationService(WebClient webClient, PayscrowProperties payscrowProperties) {
        super(webClient, Response.class);
        this.payscrowPropertiesImpl = payscrowProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/order/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("X-API-Key", payscrowPropertiesImpl.key());
        };
    }

    @Override
    protected Request body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setAmount(requisiteRequest.getAmount());
        request.setPaymentMethod(parseMethod(requisiteRequest.getMethod(), Method.class));
        request.setClientOrderId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getMethodName()) || Objects.isNull(response.getHolderAccount())) {
            log.error("Отсутствует банк либо реквизит мерчанта {} : {}", getMerchant().name(), response);
            return Optional.empty();
        }
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setMerchant(getMerchant());
        requisiteResponse.setMerchantOrderId(response.getId());
        requisiteResponse.setMerchantOrderStatus(response.getStatus().name());
        requisiteResponse.setRequisite(response.getMethodName() + " " + response.getHolderAccount());
        return Optional.of(requisiteResponse);
    }
}
