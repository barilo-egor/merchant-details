package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayHighCheckProperties;

@Service
public class ManyPayHighCheckOrderCreationService extends ManyPayOrderCreationService {


    protected ManyPayHighCheckOrderCreationService(@Qualifier("manyPayHighCheckRestClient") RestClient restClient,
                                                   ManyPayHighCheckProperties manyPayProperties, CallbackConfig callbackConfig) {
        super(restClient, manyPayProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.MANY_PAY_HIGH_CHECK;
    }

}
