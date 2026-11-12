package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.DeoraProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class DeoraMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraMerchantCreationService(@Qualifier("deoraRestClient") RestClient restClient,
                                           DeoraProperties deoraProperties, SignatureService signatureService,
                                           CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, deoraProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA;
    }
}
