package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayHighCheckProperties;

@Service
public class ManyPayHighCheckOrderCreationService extends ManyPayOrderCreationService {


    protected ManyPayHighCheckOrderCreationService(@Qualifier("manyPayHighCheckWebClient") WebClient webClient,
                                                   ManyPayHighCheckProperties manyPayProperties, CallbackConfig callbackConfig) {
        super(webClient, manyPayProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.MANY_PAY_HIGH_CHECK;
    }

}
