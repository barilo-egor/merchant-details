package net.rcetech.md.integrations.platform.settlex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.SettleX15Properties;

@Service
public class SettleX15OrderCreationService extends SettleXOrderCreationService {


    protected SettleX15OrderCreationService(@Qualifier("settleX15RestClient") RestClient restClient,
                                            SettleX15Properties settleX15Properties, CallbackConfig callbackConfig) {
        super(restClient, settleX15Properties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X_15;
    }


}
