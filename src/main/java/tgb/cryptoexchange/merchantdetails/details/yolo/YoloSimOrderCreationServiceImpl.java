package tgb.cryptoexchange.merchantdetails.details.yolo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.YoloSimProperties;

@Service
public class YoloSimOrderCreationServiceImpl extends YoloOrderCreationService {

    protected YoloSimOrderCreationServiceImpl(@Qualifier("yoloSimWebClient") WebClient webClient,
                                              YoloSimProperties yoloSimProperties, CallbackConfig callbackConfig) {
        super(webClient, yoloSimProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.YOLO_SIM;
    }

}
