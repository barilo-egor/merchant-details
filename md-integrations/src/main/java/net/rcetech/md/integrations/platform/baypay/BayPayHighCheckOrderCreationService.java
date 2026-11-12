package net.rcetech.md.integrations.platform.baypay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.BayPayHighCheckProperties;

@Service
@Slf4j
public class BayPayHighCheckOrderCreationService extends BayPayOrderCreationService {

    protected BayPayHighCheckOrderCreationService(@Qualifier("bayPayRestClient") RestClient restClient,
                                                  BayPayHighCheckProperties bayPayProperties, CallbackConfig callbackConfig) {
        super(restClient, bayPayProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BAY_PAY_HIGH_CHECK;
    }

}
