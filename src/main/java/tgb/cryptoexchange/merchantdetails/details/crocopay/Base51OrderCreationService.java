package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.Base51Properties;

@Service
@Slf4j
public class Base51OrderCreationService extends CrocoPayOrderCreationService {

    protected Base51OrderCreationService(@Qualifier("base51WebClient") WebClient webClient,
                                         Base51Properties base51Properties, CallbackConfig callbackConfig) {
        super(webClient, base51Properties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51;
    }

}
