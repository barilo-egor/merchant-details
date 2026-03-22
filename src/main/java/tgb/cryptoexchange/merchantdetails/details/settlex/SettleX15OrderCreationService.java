package tgb.cryptoexchange.merchantdetails.details.settlex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SettleX15Properties;

@Service
public class SettleX15OrderCreationService extends SettleXOrderCreationService {


    protected SettleX15OrderCreationService(@Qualifier("settleX15WebClient") WebClient webClient,
                                            SettleX15Properties settleX15Properties, CallbackConfig callbackConfig) {
        super(webClient, settleX15Properties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.SETTLE_X_15;
    }


}
