package tgb.cryptoexchange.merchantdetails.details.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.WatSimProperties;

@Service
public class WatSimOrderCreationService extends WatOrderCreationService {


    protected WatSimOrderCreationService(@Qualifier("watWebClient") WebClient webClient,
                                         WatSimProperties watSimProperties, CallbackConfig callbackConfig) {
        super(webClient, watSimProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT_SIM;
    }


}
