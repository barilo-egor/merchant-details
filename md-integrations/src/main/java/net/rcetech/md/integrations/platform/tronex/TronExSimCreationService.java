package net.rcetech.md.integrations.platform.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.TronExSimProperties;

@Service
public class TronExSimCreationService extends TronExOrderCreationService {


    protected TronExSimCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                       TronExSimProperties tronExSimProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExSimProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_SIM;
    }

}
