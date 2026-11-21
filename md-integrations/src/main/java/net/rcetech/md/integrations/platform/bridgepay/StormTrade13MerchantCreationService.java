package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.StormTrade13Properties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

@Service
public class StormTrade13MerchantCreationService extends BridgePayOrderCreationService {

    protected StormTrade13MerchantCreationService(@Qualifier("stormTrade13RestClient") RestClient restClient,
                                                  StormTrade13Properties stormTrade13Properties, SignatureService signatureService,
                                                  CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, stormTrade13Properties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STORM_TRADE_13;
    }
}
