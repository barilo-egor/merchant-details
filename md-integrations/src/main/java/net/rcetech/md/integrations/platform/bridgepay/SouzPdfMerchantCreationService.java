package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.SouzPdfProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class SouzPdfMerchantCreationService extends BridgePayOrderCreationService {

    protected SouzPdfMerchantCreationService(@Qualifier("souzPdfRestClient") RestClient restClient,
                                             SouzPdfProperties souzProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, souzProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SOUZ_PDF;
    }
}
