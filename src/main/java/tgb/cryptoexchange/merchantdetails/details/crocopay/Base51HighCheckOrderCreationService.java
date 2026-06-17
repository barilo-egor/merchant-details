package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.Base51HighCheckProperties;

@Service
@Slf4j
public class Base51HighCheckOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51HighCheckOrderCreationService(@Qualifier("base51HighCheckWebClient") WebClient webClient,
                                                  Base51HighCheckProperties base51HighCheckProperties, CallbackConfig callbackConfig) {
        super(webClient, base51HighCheckProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_HIGH_CHECK;
    }

}
