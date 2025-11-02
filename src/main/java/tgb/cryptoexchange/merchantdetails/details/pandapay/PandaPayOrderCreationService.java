package tgb.cryptoexchange.merchantdetails.details.pandapay;

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
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.PandaPayProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class PandaPayOrderCreationService extends MerchantOrderCreationService<Response> {

    private final PandaPayProperties pandaPayProperties;

    protected PandaPayOrderCreationService(@Qualifier("pandaPayWebClient") WebClient webClient,
                                           PandaPayProperties pandaPayProperties) {
        super(webClient, Response.class);
        this.pandaPayProperties = pandaPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PANDA_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/orders").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            SignatureResult signatureResult;
            try {
                signatureResult = generateSignature(body);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Ошибка формирования подписи для method={}, body={}", method().name(), body);
                throw new SignatureCreationException("Ошибка формирования подписи.", e);
            }
            httpHeaders.add("X-Timestamp", signatureResult.timestamp);
            httpHeaders.add("X-Signature", signatureResult.signature);
        };
    }

    private SignatureResult generateSignature(String payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        // Получаем текущий timestamp в секундах (UTC)
        long timestamp = Instant.now().getEpochSecond();
        String timestampStr = Long.toString(timestamp);

        // Комбинируем timestamp и payload
        String stringToSign = timestampStr + payload;

        // Создаем HMAC SHA256 хэш
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(pandaPayProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] hmacBytes = sha256Hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

        // Конвертируем в HEX строку
        String signature = HexFormat.of().formatHex(hmacBytes);

        return new SignatureResult(signature, timestampStr);
    }

    public record SignatureResult(String signature, String timestamp) {}

    @Override
    protected Object body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setPandaPayMethod(parseMethod(detailsRequest.getMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        if (Status.TRADER_NOT_FOUND.equals(response.getStatus())) {
            return Optional.empty();
        }
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setDetails(response.getRequisiteData().getBank() + " " + response.getRequisiteData().getRequisites());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchantOrderId(response.getUuid());
        return Optional.of(detailsResponse);
    }
}
