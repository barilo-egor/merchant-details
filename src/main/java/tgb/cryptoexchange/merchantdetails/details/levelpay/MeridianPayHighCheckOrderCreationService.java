package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MeridianPayHighCheckProperties;

@Service
public class MeridianPayHighCheckOrderCreationService extends LevelPayOrderCreationService {

    protected MeridianPayHighCheckOrderCreationService(@Qualifier("meridianPayHighCheckRestClient") RestClient restClient,
                                                       MeridianPayHighCheckProperties meridianPayProperties,
                                                       CallbackConfig callbackConfig) {
        super(restClient, callbackConfig, meridianPayProperties);
    }

    @Override
    protected Integer getAmount(String amount) {
        return Integer.parseInt(amount) / 100;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MERIDIAN_PAY_HIGH_CHECK;
    }
}
