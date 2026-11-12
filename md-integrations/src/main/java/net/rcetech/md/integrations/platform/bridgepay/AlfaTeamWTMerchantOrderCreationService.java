package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.AlfaTeamWTProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class AlfaTeamWTMerchantOrderCreationService extends BridgePayOrderCreationService{

    protected AlfaTeamWTMerchantOrderCreationService(@Qualifier("alfaTeamWTRestClient") RestClient restClient,
                                                     AlfaTeamWTProperties alfaTeamWTProperties,
                                                     SignatureService signatureService, CallbackConfig callbackConfig,
                                                     SleepingService sleepingService) {
        super(restClient, alfaTeamWTProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM_WT;
    }
}
