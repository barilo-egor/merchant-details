package tgb.cryptoexchange.merchantdetails.details.crocopay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayImplProperties;

@Service
public class CrocoPayImplOrderCreationService extends CrocoPayOrderCreationService {

    protected CrocoPayImplOrderCreationService(@Qualifier("crocoPayWebClient") WebClient webClient,
                                               CrocoPayImplProperties crocoPayProperties, CallbackConfig callbackConfig) {
        super(webClient, crocoPayProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CROCO_PAY;
    }

}
