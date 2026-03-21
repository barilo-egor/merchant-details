package tgb.cryptoexchange.merchantdetails.details.settlex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SettleXPropertiesImpl;

@Service
public class SettleXOrderCreationServiceImpl extends SettleXOrderCreationService {


    protected SettleXOrderCreationServiceImpl(@Qualifier("settleXWebClient") WebClient webClient,
                                              SettleXPropertiesImpl settleXProperties, CallbackConfig callbackConfig) {
        super(webClient, settleXProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X;
    }

}
