package net.rcetech.md.integrations.platform.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.HellBitReceiptProperties;

import java.net.URI;
import java.util.function.Function;

@Service
@Slf4j
public class HellBitReceiptOrderCreationService extends ReceiptOrderCreationService {

    protected HellBitReceiptOrderCreationService(@Qualifier("hellBitRestClient") RestClient restClient,
                                                 HellBitReceiptProperties hellBitProperties) {
        super(restClient, hellBitProperties);
    }

    @Override
    protected Function<UriBuilder, URI> receiptUriBuilder(String orderId) {
        return uriBuilder -> uriBuilder.pathSegment("api", "v1", "transactions", orderId, "attach").build();
    }

    @Override
    protected void addOrderIdPart(MultipartFormBuilder bodyBuilder, String orderId) {
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT_RECEIPT;
    }

}
