package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.DeoraSimProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class DeoraSimMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraSimMerchantCreationService(@Qualifier("deoraSimRestClient") RestClient restClient,
                                              DeoraSimProperties deoraProperties, SignatureService signatureService,
                                              CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, deoraProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA_SIM;
    }
}
