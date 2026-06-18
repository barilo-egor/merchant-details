package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.DeoraLowCheckProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class DeoraLowCheckMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraLowCheckMerchantCreationService(@Qualifier("deoraLowCheckWebClient") WebClient webClient,
                                                   DeoraLowCheckProperties deoraProperties, SignatureService signatureService,
                                                   CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(webClient, deoraProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA_LOW_CHECK;
    }
}
