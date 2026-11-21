package net.rcetech.md.integrations.platform.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.Base51LowCheckProperties;

@Service
@Slf4j
public class Base51LowCheckOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51LowCheckOrderCreationService(@Qualifier("base51LowCheckRestClient") RestClient restClient,
                                                 Base51LowCheckProperties base51LowCheckProperties, CallbackConfig callbackConfig) {
        super(restClient, base51LowCheckProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_LOW_CHECK;
    }

}
