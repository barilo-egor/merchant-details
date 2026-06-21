package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayPropertiesImpl;

@Service
public class ManyPayOrderCreationServiceImpl extends ManyPayOrderCreationService {


    protected ManyPayOrderCreationServiceImpl(@Qualifier("manyPayWebClient") WebClient webClient,
                                              ManyPayPropertiesImpl manyPayProperties, CallbackConfig callbackConfig) {
        super(webClient, manyPayProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.MANY_PAY;
    }

}
