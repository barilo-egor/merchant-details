package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
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
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path(method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + payBoxProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantTransactionId(UUID.randomUUID().toString());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getBankName())
                || (Objects.isNull(response.getPhoneNumber()) && Objects.isNull(response.getCardNumber()))) {
            log.error("В ответе отсутствует и номер карты, и номер телефона, либо название банка: {}", response);
            return Optional.empty();
        }
        DetailsResponse detailsResponse = getRequisiteResponse(response);
        return Optional.of(detailsResponse);
    }

    private DetailsResponse getRequisiteResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        if (Objects.nonNull(response.getPhoneNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getPhoneNumber());
        } else {
            detailsResponse.setDetails(response.getBankName() + " " + response.getCardNumber());
        }
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        return detailsResponse;
    }
}
