package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

@Service
@Slf4j
public class LotrienPDFOrderCreationService extends LotrienOrderCreationService {

    private final LotrienProperties lotrienProperties;

    protected LotrienPDFOrderCreationService(@Qualifier("lotrienWebClient") WebClient webClient,
                                             LotrienProperties lotrienProperties) {
        super(webClient, lotrienProperties);
        this.lotrienProperties = lotrienProperties;
    }


    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("orderId", orderId);
        builder.part("files", new ByteArrayResource(fileContent))
                .filename(fileName);

        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("order", "payin", "confirm").build(),
                headers -> {
                    headers.add("X-API-Key", lotrienProperties.key());
                    headers.add("Content-Type", "multipart/form-data");
                },
                BodyInserters.fromMultipartData(builder.build()),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.LOTRIEN_PDF;
    }

}
