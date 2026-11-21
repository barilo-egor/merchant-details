package net.rcetech.md.integrations.platform.manypay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.ManyPayPropertiesImpl;

@Service
public class ManyPayOrderCreationServiceImpl extends ManyPayOrderCreationService {


    protected ManyPayOrderCreationServiceImpl(@Qualifier("manyPayRestClient") RestClient restClient,
                                              ManyPayPropertiesImpl manyPayProperties, CallbackConfig callbackConfig) {
        super(restClient, manyPayProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.MANY_PAY;
    }

}
