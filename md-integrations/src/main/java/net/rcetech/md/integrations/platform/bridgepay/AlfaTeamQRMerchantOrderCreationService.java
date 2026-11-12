package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.AlfaTeamQRProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class AlfaTeamQRMerchantOrderCreationService extends BridgePayOrderCreationService {

    protected AlfaTeamQRMerchantOrderCreationService(@Qualifier("alfaTeamQRRestClient") RestClient restClient,
                                                     AlfaTeamQRProperties alfaTeamQRProperties,
                                                     SignatureService signatureService, CallbackConfig callbackConfig,
                                                     SleepingService sleepingService) {
        super(restClient, alfaTeamQRProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM_QR;
    }
}
