package net.rcetech.md.integrations.platform.settlex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.SettleXPropertiesImpl;

@Service
public class SettleXOrderCreationServiceImpl extends SettleXOrderCreationService {


    protected SettleXOrderCreationServiceImpl(@Qualifier("settleXRestClient") RestClient restClient,
                                              SettleXPropertiesImpl settleXProperties, CallbackConfig callbackConfig) {
        super(restClient, settleXProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X;
    }

}
