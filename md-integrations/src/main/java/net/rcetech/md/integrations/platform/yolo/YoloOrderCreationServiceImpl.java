package net.rcetech.md.integrations.platform.yolo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.YoloPropertiesImpl;

@Service
public class YoloOrderCreationServiceImpl extends YoloOrderCreationService {

    protected YoloOrderCreationServiceImpl(@Qualifier("yoloRestClient") RestClient restClient,
                                           YoloPropertiesImpl yoloProperties, CallbackConfig callbackConfig) {
        super(restClient, yoloProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.YOLO;
    }

}
