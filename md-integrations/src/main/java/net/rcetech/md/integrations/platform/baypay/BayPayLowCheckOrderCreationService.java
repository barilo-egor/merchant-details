package net.rcetech.md.integrations.platform.baypay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.BayPayLowCheckProperties;

@Service
@Slf4j
public class BayPayLowCheckOrderCreationService extends BayPayOrderCreationService {

    protected BayPayLowCheckOrderCreationService(@Qualifier("bayPayRestClient") RestClient restClient,
                                                 BayPayLowCheckProperties bayPayProperties, CallbackConfig callbackConfig) {
        super(restClient, bayPayProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BAY_PAY_LOW_CHECK;
    }

}
