package net.rcetech.md.integrations.platform.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayBoxProperties;

import java.net.URI;
import java.util.function.Function;

@Slf4j
public abstract class ReceiptOrderCreationService extends PayBoxOrderCreationService {

    protected ReceiptOrderCreationService(Merchant merchant, RestClient restClient,
                                          PayBoxProperties receiptProperties) {
        super(merchant, restClient, receiptProperties);
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartFormBuilder bodyBuilder = new MultipartFormBuilder();
        addOrderIdPart(bodyBuilder, orderId);
        bodyBuilder.part("receipts", new ByteArrayResource(fileContent))
                .filename(fileName)
                .contentType(MediaType.APPLICATION_PDF);
        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                receiptUriBuilder(orderId),
                headers -> headers.add("Authorization", "Bearer " + payBoxProperties.token()),
                bodyBuilder.build(),
                t -> log.error("Ошибка отправки чека мерчанту {} по ордеру {}: {}", getMerchant().getDisplayName(), orderId, t.getMessage(), t)
        );
    }

    protected Function<UriBuilder, URI> receiptUriBuilder(String orderId) {
        return uriBuilder -> uriBuilder.pathSegment("api", "v1", "transactions", "attach").build();
    }

    protected void addOrderIdPart(MultipartFormBuilder bodyBuilder, String orderId) {
        bodyBuilder.part("transaction_id", orderId, MediaType.TEXT_PLAIN);
    }

}
