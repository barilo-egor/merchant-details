package net.rcetech.md.integrations.platform.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.TronExBtProperties;

@Service
public class TronExBtCreationService extends TronExOrderCreationService {


    protected TronExBtCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                      TronExBtProperties tronExBtProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExBtProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_BT;
    }

}
