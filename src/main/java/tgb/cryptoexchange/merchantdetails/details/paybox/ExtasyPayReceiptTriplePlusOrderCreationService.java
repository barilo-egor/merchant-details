package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayReceiptTriplePlusProperties;

@Service
@Slf4j
public class ExtasyPayReceiptTriplePlusOrderCreationService extends ReceiptOrderCreationService {

    protected ExtasyPayReceiptTriplePlusOrderCreationService(
            @Qualifier("extasyPayReceiptTriplePlusWebClient") WebClient webClient,
            ExtasyPayReceiptTriplePlusProperties triplePlusProperties) {
        super(webClient, triplePlusProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_RECEIPT_3;
    }


}
