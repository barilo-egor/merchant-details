package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HellBitReceiptProperties;

import java.net.URI;
import java.util.function.Function;

@Service
@Slf4j
public class HellBitReceiptOrderCreationService extends ReceiptOrderCreationService {

    protected HellBitReceiptOrderCreationService(@Qualifier("hellBitWebClient") WebClient webClient,
                                                 HellBitReceiptProperties hellBitProperties) {
        super(webClient, hellBitProperties);
    }

    @Override
    protected Function<UriBuilder, URI> receiptUriBuilder(String orderId) {
        return uriBuilder -> uriBuilder.pathSegment("api", "v1", "transactions", orderId, "attach").build();
    }

    @Override
    protected void addOrderIdPart(MultipartBodyBuilder bodyBuilder, String orderId) {
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT_RECEIPT;
    }

}
