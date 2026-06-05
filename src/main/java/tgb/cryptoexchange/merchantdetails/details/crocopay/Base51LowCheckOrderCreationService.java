package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.Base51LowCheckProperties;

@Service
@Slf4j
public class Base51LowCheckOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51LowCheckOrderCreationService(@Qualifier("base51LowCheckWebClient") WebClient webClient,
                                                 Base51LowCheckProperties base51LowCheckProperties, CallbackConfig callbackConfig) {
        super(webClient, base51LowCheckProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_LOW_CHECK;
    }

}
