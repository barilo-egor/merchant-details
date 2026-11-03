package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.properties.PayBoxProperties;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

@Slf4j
public abstract class ReceiptOrderCreationService extends PayBoxOrderCreationService {

    protected ReceiptOrderCreationService(WebClient webClient,
                                          PayBoxProperties receiptProperties) {
        super(webClient, receiptProperties);
    }

    @Override
    public void sendReceipt(String orderId, MultipartFile multipartFile) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        byte[] fileContent;
        try {
            addOrderIdPart(bodyBuilder, orderId);
            fileContent = multipartFile.getBytes();
            String fileName = multipartFile.getOriginalFilename();
            bodyBuilder.part("receipts", new ByteArrayResource(fileContent))
                    .filename(fileName)
                    .contentType(MediaType.parseMediaType(multipartFile.getContentType()));
            requestService.request(
                    webClient,
                    HttpMethod.POST,
                    receiptUriBuilder(orderId),
                    headers -> {
                        headers.add("Authorization", "Bearer " + payBoxProperties.token());
                    },
                    BodyInserters.fromMultipartData(bodyBuilder.build()),
                    t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant().getDisplayName(), orderId, t.getMessage(), t)
            );
        } catch (IOException e) {
            log.error("Непредвиденная ошибка при подготовке чека мерчанта {} для ордера {}: {}", getMerchant().getDisplayName(), orderId, e.getMessage());
        }
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        addOrderIdPart(bodyBuilder, orderId);
        bodyBuilder.part("receipts", new ByteArrayResource(fileContent))
                .filename(fileName)
                .contentType(MediaType.APPLICATION_PDF);
        requestService.request(
                webClient,
                HttpMethod.POST,
                receiptUriBuilder(orderId),
                headers -> headers.add("Authorization", "Bearer " + payBoxProperties.token()),
                BodyInserters.fromMultipartData(bodyBuilder.build()),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant().getDisplayName(), orderId, t.getMessage(), t)
        );
    }

    protected Function<UriBuilder, URI> receiptUriBuilder(String orderId) {
        return uriBuilder -> uriBuilder.pathSegment("api", "v1", "transactions", "attach").build();
    }

    protected void addOrderIdPart(MultipartBodyBuilder bodyBuilder, String orderId) {
        bodyBuilder.part("transaction_id", orderId, MediaType.TEXT_PLAIN);
    }

}
