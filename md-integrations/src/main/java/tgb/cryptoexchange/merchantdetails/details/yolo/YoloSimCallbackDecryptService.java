package tgb.cryptoexchange.merchantdetails.details.yolo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;

@Service
@Slf4j
public class YoloSimCallbackDecryptService extends YoloCallbackDecryptService {

    public YoloSimCallbackDecryptService(CallbackConfig callbackConfig) {
        super(callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.YOLO_SIM;
    }

}
