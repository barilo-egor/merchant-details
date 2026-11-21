package net.rcetech.md.integrations.platform.crocopay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.CrocoPayImplProperties;

@Service
public class CrocoPayImplOrderCreationService extends CrocoPayOrderCreationService {

    protected CrocoPayImplOrderCreationService(@Qualifier("crocoPayRestClient") RestClient restClient,
                                               CrocoPayImplProperties crocoPayProperties, CallbackConfig callbackConfig) {
        super(restClient, crocoPayProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CROCO_PAY;
    }

}
