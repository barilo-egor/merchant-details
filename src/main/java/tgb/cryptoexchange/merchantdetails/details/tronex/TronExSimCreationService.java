package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.TronExSimProperties;

@Service
public class TronExSimCreationService extends TronExOrderCreationService {


    protected TronExSimCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                       TronExSimProperties tronExSimProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExSimProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_SIM;
    }

}
