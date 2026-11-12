package net.rcetech.md.integrations.platform.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.Base51SimProperties;

@Service
@Slf4j
public class Base51SimOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51SimOrderCreationService(@Qualifier("base51SimRestClient") RestClient restClient,
                                            Base51SimProperties base51SimProperties, CallbackConfig callbackConfig) {
        super(restClient, base51SimProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_SIM;
    }

}
