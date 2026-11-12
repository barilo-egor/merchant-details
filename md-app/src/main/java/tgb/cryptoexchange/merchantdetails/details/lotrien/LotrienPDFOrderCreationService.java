package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import tgb.cryptoexchange.merchantdetails.util.MultipartFormBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

@Service
@Slf4j
public class LotrienPDFOrderCreationService extends LotrienOrderCreationService {

    private final LotrienProperties lotrienProperties;

    protected LotrienPDFOrderCreationService(@Qualifier("lotrienRestClient") RestClient restClient,
                                             LotrienProperties lotrienProperties) {
        super(restClient, lotrienProperties);
        this.lotrienProperties = lotrienProperties;
    }


    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("orderId", orderId);
        builder.part("files", new ByteArrayResource(fileContent))
                .filename(fileName)
                .contentType(MediaType.APPLICATION_PDF);

        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("order", "payin", "confirm").build(),
                headers -> headers.add("X-API-Key", lotrienProperties.key()),
                builder.build(),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant(), orderId, t.getMessage(), t)
        );
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.LOTRIEN_PDF;
    }

}
