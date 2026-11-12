package net.rcetech.md.integrations.platform.yolo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.YoloSimProperties;

@Service
public class YoloSimOrderCreationServiceImpl extends YoloOrderCreationService {

    protected YoloSimOrderCreationServiceImpl(@Qualifier("yoloSimRestClient") RestClient restClient,
                                              YoloSimProperties yoloSimProperties, CallbackConfig callbackConfig) {
        super(restClient, yoloSimProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.YOLO_SIM;
    }

}
