package tgb.cryptoexchange.merchantdetails.details.zpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.ZPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class ZPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final ZPayProperties zPayProperties;

    protected ZPayOrderCreationService(@Qualifier("zPayWebClient") WebClient webClient,
                                       ZPayProperties zPayProperties) {
        super(webClient, Response.class, Callback.class);
        this.zPayProperties = zPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.Z_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/merchant/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + zPayProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethodType(parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderStatus(Status.INITIATED.name());
        requisiteVO.setMerchantOrderId(String.valueOf(response.getId()));
        requisiteVO.setDetails(response.getBankName() + " " + response.getNumber());
        return Optional.of(requisiteVO);
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("deal_id", Integer.parseInt(orderId));
        bodyBuilder.part("file", new ByteArrayResource(fileContent))
                .filename(fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("merchant", "disputes").build(),
                headers -> headers.add("Authorization", "Bearer " + zPayProperties.token()),
                BodyInserters.fromMultipartData(bodyBuilder.build()),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant().getDisplayName(), orderId, t.getMessage(), t)
        );
    }
}
