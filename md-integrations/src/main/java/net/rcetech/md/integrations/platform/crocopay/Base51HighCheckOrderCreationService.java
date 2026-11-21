package net.rcetech.md.integrations.platform.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.Base51HighCheckProperties;

@Service
@Slf4j
public class Base51HighCheckOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51HighCheckOrderCreationService(@Qualifier("base51HighCheckRestClient") RestClient restClient,
                                                  Base51HighCheckProperties base51HighCheckProperties, CallbackConfig callbackConfig) {
        super(restClient, base51HighCheckProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_HIGH_CHECK;
    }

}
