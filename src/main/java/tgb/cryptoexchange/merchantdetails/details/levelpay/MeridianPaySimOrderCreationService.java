package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MeridianPaySimProperties;

@Service
public class MeridianPaySimOrderCreationService extends LevelPayOrderCreationService {

    protected MeridianPaySimOrderCreationService(@Qualifier("meridianPaySimWebClient") WebClient webClient,
                                                 MeridianPaySimProperties meridianPayProperties,
                                                 CallbackConfig callbackConfig) {
        super(webClient, callbackConfig, meridianPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MERIDIAN_PAY_SIM;
    }
}
