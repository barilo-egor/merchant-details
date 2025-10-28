package tgb.cryptoexchange.merchantdetails.details.creation.whitelabel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.dto.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.dto.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.details.whitelabel.*;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.WhiteLabelProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class WhiteLabelOrderCreationService extends MerchantOrderCreationService<Response> {

    private final String createInvoiceUrl;

    private final WhiteLabelProperties whiteLabelProperties;

    private final ObjectMapper objectMapper;

    protected WhiteLabelOrderCreationService(WebClient webClient, WhiteLabelProperties whiteLabelProperties,
                                             ObjectMapper objectMapper) {
        super(webClient);
        this.whiteLabelProperties = whiteLabelProperties;
        this.createInvoiceUrl = whiteLabelProperties.url() + "/api/merchant/invoices";
        this.objectMapper = objectMapper;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder() {
        return uriBuilder -> uriBuilder.path("/api/merchant/invoices").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest) {
        return headers -> {
            headers.add("Content-Type", "application/json");
            headers.add("X-Identity", whiteLabelProperties.key());
            String body;
            try {
                body = body(requisiteRequest);
            } catch (JsonProcessingException e) {
                throw new BodyMappingException("Ошибка парсинга тела запроса.", e);
            }
            try {
                headers.add("X-Signature", generateXSignature(createInvoiceUrl, body));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Ошибка формирования подписи для method={}, url={}, body={}", method().name(), createInvoiceUrl, body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
        };
    }

    private String generateXSignature(String url, String body) throws NoSuchAlgorithmException, InvalidKeyException {
        String data = method().name().toUpperCase() + url + (body != null ? body : "");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(whiteLabelProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(rawHmac);
    }

    @Override
    protected String body(RequisiteRequest requisiteRequest) throws JsonProcessingException {
        PaymentOption paymentOption = PaymentOption.valueOf(requisiteRequest.getMethod());
        Request request = new Request();
        request.setAmount(requisiteRequest.getAmount().toString());
        request.setCurrency(FiatCurrency.RUB.name());
        request.setNotificationUrl(requisiteRequest.getCallbackUrl());
        request.setNotificationToken(whiteLabelProperties.token());
        request.setInternalId(UUID.randomUUID().toString());
        request.setPaymentOption(paymentOption);
        request.setStartDeal(true);
        return objectMapper.writeValueAsString(request);
    }

    @Override
    protected Optional<RequisiteResponse> mapToRequisiteDTO(Response response) {
        if (!response.hasRequisites()) {
            return Optional.empty();
        }
        RequisiteResponse requisiteVO = new RequisiteResponse();
        String invoiceId = response.getId();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(invoiceId);
        requisiteVO.setMerchantOrderStatus(InvoiceStatus.NEW.name());
        requisiteVO.setRequisite(buildRequisite(response));
        return Optional.of(requisiteVO);
    }

    private String buildRequisite(Response response) {
        DealDTO dealDTO = response.getDeals().getFirst();
        if (StringUtils.isNotBlank(dealDTO.getPaymentMethod().getDisplayName())) {
            return dealDTO.getPaymentMethod().getDisplayName() + " " + dealDTO.getRequisites().getRequisites();
        }
        return dealDTO.getRequisites().getRequisites();
    }

}
