package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SouzSbpQrProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class SouzSbpQrMerchantCreationService extends BridgePayOrderCreationService {

    protected SouzSbpQrMerchantCreationService(@Qualifier("souzSbpQrWebClient") WebClient webClient,
                                               SouzSbpQrProperties souzProperties, SignatureService signatureService,
                                               CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(webClient, souzProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SOUZ_SBP_QR;
    }
}
