package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayReceiptProperties;

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
        bodyBuilder.part("transaction_id", orderId);
        bodyBuilder.part("receipts", multipartFile.getResource());

        requestService.request(
                webClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("attach").build(),
                headers -> {
                    headers.add("Authorization", "Bearer " + payBoxProperties.token());
                    headers.add("Content-Type", "multipart/form-data");
                },
                BodyInserters.fromMultipartData(bodyBuilder.build()),
                t -> log.error("Ошибка отправки чека мерчанту EXTASY_PAY_RECEIPT по ордеру {}: {}", orderId, t.getMessage(), t)
        );
    }


}
