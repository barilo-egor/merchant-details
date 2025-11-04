package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.BridgePayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class BridgePayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final BridgePayProperties bridgePayProperties;

    private final SignatureService signatureService;

    protected BridgePayOrderCreationService(WebClient webClient, BridgePayProperties bridgePayProperties,
                                            SignatureService signatureService) {
        super(webClient, Response.class);
        this.bridgePayProperties = bridgePayProperties;
        this.signatureService = signatureService;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/merchant/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return headers -> {
            headers.add("Content-Type", "application/json");
            headers.add("X-Identity", bridgePayProperties.key());
            String createInvoiceUrl = bridgePayProperties.url() + "/api/merchant/invoices";
            try {
                headers.add("X-Signature", signatureService.hmacSHA1(
                        buildSignatureData(createInvoiceUrl, body), bridgePayProperties.secret()
                ));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Ошибка формирования подписи для method={}, url={}, body={}", method().name(), createInvoiceUrl, body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    private String buildSignatureData(String url, String body) {
        return method().name().toUpperCase() + url + (body != null ? body : "");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        request.setCurrency(FiatCurrency.RUB.name());
        request.setNotificationUrl(detailsRequest.getCallbackUrl());
        request.setNotificationToken(bridgePayProperties.token());
        request.setInternalId(UUID.randomUUID().toString());
        request.setPaymentOption(parseMethod(detailsRequest.getMethod(), Method.class));
        request.setStartDeal(true);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        String invoiceId = response.getId();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(invoiceId);
        requisiteVO.setMerchantOrderStatus(InvoiceStatus.NEW.name());
        requisiteVO.setDetails(buildRequisite(response));
        return Optional.of(requisiteVO);
    }

    private String buildRequisite(Response response) {
        DealDTO dealDTO = response.getDeals().getFirst();
        return dealDTO.getPaymentMethod().getDisplayName() + " " + dealDTO.getRequisites().getRequisites();
    }

}
