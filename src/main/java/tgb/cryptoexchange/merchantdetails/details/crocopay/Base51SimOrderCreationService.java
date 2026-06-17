package tgb.cryptoexchange.merchantdetails.details.crocopay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.Base51SimProperties;

@Service
@Slf4j
public class Base51SimOrderCreationService extends CrocoPayOrderCreationService {

    protected Base51SimOrderCreationService(@Qualifier("base51SimWebClient") WebClient webClient,
                                            Base51SimProperties base51SimProperties, CallbackConfig callbackConfig) {
        super(webClient, base51SimProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BASE_51_SIM;
    }

}
