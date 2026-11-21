package net.rcetech.md.integrations.platform.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.WatSimProperties;
import net.rcetech.md.integrations.service.ReceiptService;

@Service
public class WatSimOrderCreationService extends WatOrderCreationService {


    protected WatSimOrderCreationService(@Qualifier("watRestClient") RestClient restClient,
                                         WatSimProperties watSimProperties, CallbackConfig callbackConfig,
                                         ReceiptService receiptService) {
        super(restClient, watSimProperties, callbackConfig, receiptService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT_SIM;
    }


}
