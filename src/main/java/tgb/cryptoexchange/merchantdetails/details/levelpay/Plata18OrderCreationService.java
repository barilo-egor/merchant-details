package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.Plata18Properties;

@Service
public class Plata18OrderCreationService extends LevelPayOrderCreationService {

    protected Plata18OrderCreationService(@Qualifier("plata18WebClient") WebClient webClient,
                                               CallbackConfig callbackConfig, Plata18Properties plata18Properties) {
        super(webClient, callbackConfig, plata18Properties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PLATA_18;
    }
}
