package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayReceiptProperties;

import java.io.IOException;

@Service
@Slf4j
public class ExtasyPayReceiptOrderCreationService extends PayBoxOrderCreationService {

    protected ExtasyPayReceiptOrderCreationService(@Qualifier("extasyPayReceiptWebClient") WebClient webClient,
                                                   ExtasyPayReceiptProperties extasyPayReceiptProperties) {
        super(webClient, extasyPayReceiptProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_RECEIPT;
    }

    @Override
    public void sendReceipt(String orderId, MultipartFile multipartFile) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        byte[] fileContent;
        try {
            bodyBuilder.part("transaction_id", orderId, MediaType.TEXT_PLAIN);
            fileContent = multipartFile.getBytes();
            String fileName = multipartFile.getOriginalFilename();
            bodyBuilder.part("receipts", new ByteArrayResource(fileContent))
                    .filename(fileName)
                    .contentType(MediaType.parseMediaType(multipartFile.getContentType()));
            requestService.request(
                    webClient,
                    HttpMethod.POST,
                    uriBuilder -> uriBuilder.pathSegment("api", "v1", "transactions", "attach").build(),
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


}
