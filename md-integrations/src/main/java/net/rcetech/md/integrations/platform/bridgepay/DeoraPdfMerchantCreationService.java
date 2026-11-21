package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.DeoraPdfProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class DeoraPdfMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraPdfMerchantCreationService(@Qualifier("deoraPdfRestClient") RestClient restClient,
                                              DeoraPdfProperties deoraProperties, SignatureService signatureService,
                                              CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, deoraProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA_PDF;
    }
}
