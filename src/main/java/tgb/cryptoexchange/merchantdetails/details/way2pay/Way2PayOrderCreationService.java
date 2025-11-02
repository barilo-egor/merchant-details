package tgb.cryptoexchange.merchantdetails.details.way2pay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.Way2PayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class Way2PayOrderCreationService extends MerchantOrderCreationService<Response> {

    private static final String CREATE_ORDER_URI = "/api/v1/pay-in";

    private final Way2PayProperties way2PayProperties;

    private final ObjectMapper objectMapper;

    private final SignatureService signatureService;

    protected Way2PayOrderCreationService(@Qualifier("way2payWebClient") WebClient webClient,
                                          Way2PayProperties way2PayProperties, ObjectMapper objectMapper,
                                          SignatureService signatureService) {
        super(webClient, Response.class);
        this.way2PayProperties = way2PayProperties;
        this.objectMapper = objectMapper;
        this.signatureService = signatureService;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.WAY_2_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path(CREATE_ORDER_URI).build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            String nonce;
            try {
                nonce = objectMapper.readValue(body, Request.class).getExternalID();
            } catch (JsonProcessingException e) {
                log.error("Ошибка парсинга тела запроса мерчанта {} : {}", getMerchant().name(), body);
                throw new BodyMappingException("Ошибка парсинга тела запроса.");
            }
            httpHeaders.add("nonce", nonce);
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Public-Key", way2PayProperties.pub());
            httpHeaders.add("X-Environment", way2PayProperties.environment());
            try {
                httpHeaders.add("Signature", signatureService.generateHmacSha512Signature(
                        CREATE_ORDER_URI + body + nonce, way2PayProperties.secret()
                ));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Ошибка формирования подписи для method={}, body={}", method().name(), body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        request.setBankId(Method.CARD.equals(method) ? 1 : 2);
        request.setCallbackUrl(detailsRequest.getCallbackUrl());
        request.setMethod(method);
        request.setCurrencyId(1);
        String nonce = System.currentTimeMillis() + "00000";
        request.setExternalID(nonce);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getData().getId());
        detailsResponse.setMerchantOrderStatus(response.getData().getStatus().name());
        detailsResponse.setDetails(response.getData().getBank() + " " + response.getData().getReceiver());
        return Optional.of(detailsResponse);
    }
}
