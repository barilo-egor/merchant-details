package tgb.cryptoexchange.merchantdetails.details.yolo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.YoloPropertiesImpl;

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
