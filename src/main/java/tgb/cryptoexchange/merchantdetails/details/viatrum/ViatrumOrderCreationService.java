package tgb.cryptoexchange.merchantdetails.details.viatrum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.ViatrumProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class ViatrumOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private static final String CREATE_ORDER_URI = "/api/v1/pay-in";

    private final ViatrumProperties viatrumProperties;

    private final ObjectMapper objectMapper;

    private final SignatureService signatureService;

    private final CallbackConfig callbackConfig;

    protected ViatrumOrderCreationService(@Qualifier("viatrumWebClient") WebClient webClient,
                                          ViatrumProperties viatrumProperties, ObjectMapper objectMapper,
                                          SignatureService signatureService, CallbackConfig callbackConfig) {
        super(webClient, Response.class, Callback.class);
        this.viatrumProperties = viatrumProperties;
        this.objectMapper = objectMapper;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.VIATRUM;
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
            httpHeaders.add("Public-Key", viatrumProperties.pub());
            httpHeaders.add("X-Environment", viatrumProperties.environment());
            try {
                httpHeaders.add("Signature", signatureService.generateHmacSha512Signature(
                        CREATE_ORDER_URI + body + nonce, viatrumProperties.secret()
                ));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Ошибка формирования подписи для method={}, body={}", method().name(), body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Method method = parseMethod(detailsRequest, Method.class);
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount().toString());
        request.setBankId(method.getId());
        request.setCallbackUrl(callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name()
                + "&secret=" + callbackConfig.getCallbackSecret());
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
