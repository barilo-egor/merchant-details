package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MeridianPayLowCheckProperties;

@Service
public class MeridianPayLowCheckOrderCreationService extends LevelPayOrderCreationService {

    protected MeridianPayLowCheckOrderCreationService(@Qualifier("meridianPayLowCheckWebClient") WebClient webClient,
                                                      MeridianPayLowCheckProperties meridianPayProperties,
                                                      CallbackConfig callbackConfig) {
        super(webClient, callbackConfig, meridianPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MERIDIAN_PAY_LOW_CHECK;
    }
}
