package net.rcetech.md.integrations.platform.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.ExtasyPayReceiptProperties;

@Service
@Slf4j
public class ExtasyPayReceiptOrderCreationService extends ReceiptOrderCreationService {

    protected ExtasyPayReceiptOrderCreationService(@Qualifier("extasyPayReceiptRestClient") RestClient restClient,
                                                   ExtasyPayReceiptProperties extasyPayReceiptProperties) {
        super(restClient, extasyPayReceiptProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_RECEIPT;
    }


}
