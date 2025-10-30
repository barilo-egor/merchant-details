package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.properties.PayBoxProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class PayBoxOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PayBoxProperties payBoxProperties;

    protected PayBoxOrderCreationService(WebClient webClient, PayBoxProperties payBoxProperties) {
        super(webClient, Response.class);
        this.payBoxProperties = payBoxProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        Method method = parseMethod(requisiteRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path(method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + payBoxProperties.token());
        };
    }

    @Override
    protected Request body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setAmount(requisiteRequest.getAmount());
        request.setMerchantTransactionId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (response.hasErrors()) {
            log.error("Ошибки в ответе на запрос от мерчанта {}: {}", getMerchant().name(), response);
            return Optional.empty();
        }
        if (Objects.isNull(response.getBankName())
                || (Objects.isNull(response.getPhoneNumber()) && Objects.isNull(response.getCardNumber()))) {
            log.error("В ответе отсутствует и номер карты, и номер телефона, либо название банка: {}", response);
            return Optional.empty();
        }
        RequisiteResponse requisiteResponse = getRequisiteResponse(response);
        return Optional.of(requisiteResponse);
    }

    private RequisiteResponse getRequisiteResponse(Response response) {
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setMerchant(getMerchant());
        requisiteResponse.setMerchantOrderId(response.getId().toString());
        if (Objects.nonNull(response.getPhoneNumber())) {
            requisiteResponse.setRequisite(response.getBankName() + " " + response.getPhoneNumber());
        } else {
            requisiteResponse.setRequisite(response.getBankName() + " " + response.getCardNumber());
        }
        requisiteResponse.setMerchantOrderStatus(response.getStatus().name());
        return requisiteResponse;
    }
}
