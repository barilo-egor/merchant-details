package net.rcetech.md.integrations.platform.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.Base51Properties;

@Service
@Slf4j
public class Base51OrderCreationService extends CrocoPayOrderCreationService {

    protected Base51OrderCreationService(@Qualifier("base51RestClient") RestClient restClient,
                                         Base51Properties base51Properties, CallbackConfig callbackConfig) {
        super(restClient, base51Properties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51;
    }

}
