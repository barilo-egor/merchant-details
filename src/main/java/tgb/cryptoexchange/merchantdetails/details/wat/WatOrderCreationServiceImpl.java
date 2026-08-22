package tgb.cryptoexchange.merchantdetails.details.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.WatPropertiesImpl;

@Service
public class WatOrderCreationServiceImpl extends WatOrderCreationService {


    protected WatOrderCreationServiceImpl(@Qualifier("watWebClient") WebClient webClient,
                                          WatPropertiesImpl watProperties, CallbackConfig callbackConfig) {
        super(webClient, watProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT;
    }

}
