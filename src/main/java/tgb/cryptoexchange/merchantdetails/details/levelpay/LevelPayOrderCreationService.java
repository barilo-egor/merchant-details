package tgb.cryptoexchange.merchantdetails.details.levelpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.properties.LevelPayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class LevelPayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final LevelPayProperties levelPayProperties;

    protected LevelPayOrderCreationService(WebClient webClient, LevelPayProperties levelPayProperties) {
        super(webClient, Response.class);
        this.levelPayProperties = levelPayProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        return uriBuilder -> uriBuilder.path("/api/h2h/order").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Accept", "application/json");
            httpHeaders.add("Access-Token", levelPayProperties.token());
        };
    }

    @Override
    protected Object body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setMerchantId(levelPayProperties.merchantId());
        request.setAmount(requisiteRequest.getAmount());
        request.setPaymentDetailType(parseMethod(requisiteRequest.getMethod(), Method.class));
        request.setExternalId(UUID.randomUUID().toString());
        request.setCallbackUrl(request.getCallbackUrl());
        request.setFloatingAmount(true);
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getData()) || Objects.isNull(response.getData().getPaymentGatewayName())
                || Objects.isNull(response.getData().getPaymentDetail())) {
            log.debug("Отсутствуют реквизиты в ответе мерчанта {}: {}", getMerchant().name(), response);
            return Optional.empty();
        }
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setRequisite(response.getData().getPaymentGatewayName() + " " + response.getData().getPaymentDetail().getDetail());
        requisiteResponse.setMerchant(getMerchant());
        requisiteResponse.setMerchantOrderId(response.getData().getOrderId());
        requisiteResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        requisiteResponse.setAmount(Integer.parseInt(response.getData().getAmount()));
        return Optional.of(requisiteResponse);
    }
}
