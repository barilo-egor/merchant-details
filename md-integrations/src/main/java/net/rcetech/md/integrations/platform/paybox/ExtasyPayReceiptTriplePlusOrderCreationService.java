package net.rcetech.md.integrations.platform.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.ExtasyPayReceiptTriplePlusProperties;

@Service
@Slf4j
public class ExtasyPayReceiptTriplePlusOrderCreationService extends ReceiptOrderCreationService {

    protected ExtasyPayReceiptTriplePlusOrderCreationService(
            @Qualifier("extasyPayReceiptTriplePlusRestClient") RestClient restClient,
            ExtasyPayReceiptTriplePlusProperties triplePlusProperties) {
        super(restClient, triplePlusProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_RECEIPT_3;
    }


}
