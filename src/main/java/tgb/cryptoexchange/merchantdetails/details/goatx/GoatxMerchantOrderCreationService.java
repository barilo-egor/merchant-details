package tgb.cryptoexchange.merchantdetails.details.goatx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.GoatxProperties;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class GoatxMerchantOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final GoatxProperties goatxProperties;

    protected GoatxMerchantOrderCreationService(@Qualifier("goatxWebClient") WebClient webClient,
                                                GoatxProperties goatxProperties) {
        super(webClient, Response.class, Callback.class);
        this.goatxProperties = goatxProperties;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/order/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setSum(detailsRequest.getAmount().toString());
        request.setContract(goatxProperties.merchantContractId());
        request.setWay(parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class));
        request.setInvid(UUID.randomUUID().toString());
        request.setSignature(generateSignature(goatxProperties.login(), request.getSum(), request.getInvid(), goatxProperties.apiKey()));
        return request;
    }

    private String generateSignature(String login, String sum, String invid, String apiKey) {
        try {
            String input = String.format("%s:%s:%s:%s", login, sum, invid, apiKey);
            log.debug("input goatx string: {}", input);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            log.debug("hex string: {}", hexString);
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureCreationException("Ошибка алгоритма хеширования", e);
        }
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId());
        if (Method.CARD.equals(response.getWay())) {
            detailsResponse.setDetails(response.getRequisite().getBank().getName() + " " + response.getRequisite().getCardNumber());
        } else {
            detailsResponse.setDetails(response.getRequisite().getBank().getName() + " " + response.getRequisite().getPhoneNumber());
        }
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GOAT_X;
    }
}
