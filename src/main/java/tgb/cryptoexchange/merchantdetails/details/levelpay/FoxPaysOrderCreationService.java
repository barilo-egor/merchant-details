package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.FoxPaysProperties;

@Service
public class FoxPaysOrderCreationService extends LevelPayOrderCreationService {

    protected FoxPaysOrderCreationService(@Qualifier("foxPaysWebClient") WebClient webClient,
                                          FoxPaysProperties foxPaysProperties,
                                          CallbackConfig callbackConfig) {
        super(webClient, callbackConfig, foxPaysProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.FOX_PAYS;
    }
}
