package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayLowCheckProperties;

@Service
public class ManyPayLowCheckOrderCreationService extends ManyPayOrderCreationService {


    protected ManyPayLowCheckOrderCreationService(@Qualifier("manyPayLowCheckRestClient") RestClient restClient,
                                                  ManyPayLowCheckProperties manyPayProperties, CallbackConfig callbackConfig) {
        super(restClient, manyPayProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.MANY_PAY_LOW_CHECK;
    }

}
