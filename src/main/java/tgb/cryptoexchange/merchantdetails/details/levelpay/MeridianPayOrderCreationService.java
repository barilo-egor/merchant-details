package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MeridianPayProperties;

@Service
public class MeridianPayOrderCreationService extends LevelPayOrderCreationService {

    protected MeridianPayOrderCreationService(@Qualifier("meridianPayWebClient") WebClient webClient,
                                              MeridianPayProperties meridianPayProperties,
                                              CallbackConfig callbackConfig) {
        super(webClient, callbackConfig, meridianPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MERIDIAN_PAY;
    }
}
