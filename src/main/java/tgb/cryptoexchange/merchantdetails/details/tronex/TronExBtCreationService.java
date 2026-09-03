package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.TronExBtProperties;

@Service
public class TronExBtCreationService extends TronExOrderCreationService {


    protected TronExBtCreationService(@Qualifier("tronExWebClient") WebClient webClient,
                                      TronExBtProperties tronExBtProperties, CallbackConfig callbackConfig) {
        super(webClient, tronExBtProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_BT;
    }

}
