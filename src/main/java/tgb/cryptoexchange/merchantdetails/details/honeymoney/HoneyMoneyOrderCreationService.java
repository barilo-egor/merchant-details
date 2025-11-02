package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HoneyMoneyProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class HoneyMoneyOrderCreationService extends MerchantOrderCreationService<Response> {

    private final HoneyMoneyProperties honeyMoneyProperties;

    private final SignatureService signatureService;

    protected HoneyMoneyOrderCreationService(@Qualifier("honeyMoneyWebClient") WebClient webClient,
                                             HoneyMoneyProperties honeyMoneyProperties, SignatureService signatureService) {
        super(webClient, Response.class);
        this.honeyMoneyProperties = honeyMoneyProperties;
        this.signatureService = signatureService;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HONEY_MONEY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path(method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + honeyMoneyProperties.authToken());
            httpHeaders.add("Content-Type", "application/json");
            Method method = parseMethod(detailsRequest.getMethod(), Method.class);
            httpHeaders.add("X-Signature", signatureService.hmacSHA256(body, URI.create(honeyMoneyProperties.url() + method.getUri()), honeyMoneyProperties.signToken()));
        };
    }

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setExtId(UUID.randomUUID().toString());
        request.setBank(parseMethod(detailsRequest.getMethod(), Method.class).getBank());
        request.setCallbackUrl(detailsRequest.getCallbackUrl());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getPhoneNumber()) && Objects.isNull(response.getCardNumber())) {
            log.error("Не найден ни номер телефона, ни номер карты в реквизитах мерчанта {} : {}", getMerchant().name(), response);
        }
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);
        String requisite = Objects.nonNull(response.getPhoneNumber()) ? response.getPhoneNumber() : response.getCardNumber();
        detailsResponse.setDetails(response.getBankName() + " " + requisite);
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        detailsResponse.setMerchantOrderStatus(Status.PENDING.name());
        return Optional.of(detailsResponse);
    }
}
