package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MobiusProperties;

@Service
public class MobiusOrderCreationService extends LevelPayOrderCreationService {

    protected MobiusOrderCreationService(@Qualifier("mobiusWebClient") WebClient webClient,
                                         MobiusProperties mobiusProperties, CallbackConfig callbackConfig) {
        super(webClient, callbackConfig, mobiusProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MOBIUS;
    }
}
