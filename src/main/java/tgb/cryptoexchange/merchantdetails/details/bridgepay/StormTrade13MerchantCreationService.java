package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.StormTrade13Properties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class StormTrade13MerchantCreationService extends BridgePayOrderCreationService {

    protected StormTrade13MerchantCreationService(@Qualifier("stormTrade13WebClient") WebClient webClient,
                                                  StormTrade13Properties stormTrade13Properties, SignatureService signatureService,
                                                  CallbackConfig callbackConfig) {
        super(webClient, stormTrade13Properties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STORM_TRADE_13;
    }
}
