package tgb.cryptoexchange.merchantdetails.details.settlex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SettleXPropertiesImpl;

@Service
public class SettleXOrderCreationServiceImpl extends SettleXOrderCreationService {


    protected SettleXOrderCreationServiceImpl(@Qualifier("settleXRestClient") RestClient restClient,
                                              SettleXPropertiesImpl settleXProperties, CallbackConfig callbackConfig) {
        super(restClient, settleXProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X;
    }

}
