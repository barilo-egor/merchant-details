package net.rcetech.md.integrations.platform.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.TronExQrProperties;

@Service
public class TronExQrCreationService extends TronExOrderCreationService {


    protected TronExQrCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                      TronExQrProperties tronExQrProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExQrProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_QR;
    }

}
