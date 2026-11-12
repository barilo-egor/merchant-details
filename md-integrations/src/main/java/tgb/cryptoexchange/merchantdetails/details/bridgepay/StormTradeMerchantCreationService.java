package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.StormTradeProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class StormTradeMerchantCreationService extends BridgePayOrderCreationService {

    protected StormTradeMerchantCreationService(@Qualifier("stormTradeRestClient") RestClient restClient,
                                                StormTradeProperties stormTradeProperties, SignatureService signatureService,
                                                CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, stormTradeProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STORM_TRADE;
    }
}
