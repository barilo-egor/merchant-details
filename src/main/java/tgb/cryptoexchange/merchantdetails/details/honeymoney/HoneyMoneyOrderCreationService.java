package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
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
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        Method method = parseMethod(requisiteRequest.getMethod(), Method.class);
        return uriBuilder -> uriBuilder.path(method.getUri()).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Bearer " + honeyMoneyProperties.authToken());
            httpHeaders.add("Content-Type", "application/json");
            Method method = parseMethod(requisiteRequest.getMethod(), Method.class);
            httpHeaders.add("X-Signature", signatureService.hmacSHA256(body, URI.create(honeyMoneyProperties.url() + method.getUri()), honeyMoneyProperties.signToken()));
        };
    }

    @Override
    protected Object body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setAmount(requisiteRequest.getAmount());
        request.setExtId(UUID.randomUUID().toString());
        request.setBank(parseMethod(requisiteRequest.getMethod(), Method.class).getBank());
        request.setCallbackUrl(requisiteRequest.getCallbackUrl());
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (Objects.isNull(response.getPhoneNumber()) && Objects.isNull(response.getCardNumber())) {
            log.error("Не найден ни номер телефона, ни номер карты в реквизитах мерчанта {} : {}", getMerchant().name(), response);
        }
        RequisiteResponse requisiteResponse = new RequisiteResponse();
        requisiteResponse.setMerchant(Merchant.HONEY_MONEY);
        String requisite = Objects.nonNull(response.getPhoneNumber()) ? response.getPhoneNumber() : response.getCardNumber();
        requisiteResponse.setRequisite(response.getBankName() + " " + requisite);
        requisiteResponse.setMerchant(getMerchant());
        requisiteResponse.setMerchantOrderId(response.getId().toString());
        requisiteResponse.setMerchantOrderStatus(Status.PENDING.name());
        return Optional.of(requisiteResponse);
    }
}
