package tgb.cryptoexchange.merchantdetails.details.paycrown;

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
import tgb.cryptoexchange.merchantdetails.properties.PayCrownProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PayCrownOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PayCrownProperties payCrownProperties;

    private final SignatureService signatureService;

    private final ObjectMapper objectMapper;

    protected PayCrownOrderCreationService(@Qualifier("payCrownWebClient") WebClient webClient,
                                           PayCrownProperties payCrownProperties, SignatureService signatureService,
                                           ObjectMapper objectMapper) {
        super(webClient, Response.class);
        this.payCrownProperties = payCrownProperties;
        this.signatureService = signatureService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_CROWN;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/order/deposit").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            Long unixTime;
            try {
                unixTime = objectMapper.readValue(body, Request.class).getCreatedAt();
            } catch (JsonProcessingException e) {
                log.error("Ошибка парсинга тела запроса мерчанта {} : {}", getMerchant().name(), body);
                throw new BodyMappingException("Ошибка парсинга тела запроса.");
            }
            Method method = parseMethod(detailsRequest.getMethod(), Method.class);
            try {
                String signature = signatureService.getMD5Hash(detailsRequest.getAmount() + unixTime + "rub"
                        + payCrownProperties.merchantId() + method.getValue() + payCrownProperties.secret());
                httpHeaders.add("X-Api-Key", payCrownProperties.key());
                httpHeaders.add("X-Paycrown-Sign", signature);
            } catch (NoSuchAlgorithmException e) {
                log.error("Ошибка формирования подписи для method={}, body={}", method().name(), body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantId(payCrownProperties.merchantId());
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        request.setMethod(method);
        request.setCallbackUrl(detailsRequest.getCallbackUrl());
        Long unixTime = System.currentTimeMillis() / 1000L;
        request.setCreatedAt(unixTime);
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderStatus(Status.NEW.name());
        requisiteVO.setMerchantOrderId(response.getData().getId());
        requisiteVO.setDetails(response.getData().getRequisites().getBank() + " " + response.getData().getRequisites().getRequisitesString());
        return Optional.of(requisiteVO);
    }
}
