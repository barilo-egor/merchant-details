package net.rcetech.md.integrations.platform.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.WatPropertiesImpl;
import net.rcetech.md.integrations.service.ReceiptService;

@Service
public class WatOrderCreationServiceImpl extends WatOrderCreationService {


    protected WatOrderCreationServiceImpl(@Qualifier("watRestClient") RestClient restClient,
                                          WatPropertiesImpl watProperties, CallbackConfig callbackConfig,
                                          ReceiptService receiptService) {
        super(restClient, watProperties, callbackConfig, receiptService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT;
    }

}
