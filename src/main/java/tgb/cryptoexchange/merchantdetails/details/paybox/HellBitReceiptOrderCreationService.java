package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HellBitReceiptProperties;

@Service
@Slf4j
public class HellBitReceiptOrderCreationService extends ReceiptOrderCreationService {

    protected HellBitReceiptOrderCreationService(@Qualifier("hellBitWebClient") WebClient webClient,
                                                 HellBitReceiptProperties hellBitProperties) {
        super(webClient, hellBitProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT_RECEIPT;
    }

}
