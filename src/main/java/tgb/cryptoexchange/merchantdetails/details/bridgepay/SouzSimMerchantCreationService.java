package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SouzSimProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class SouzSimMerchantCreationService extends BridgePayOrderCreationService {

    protected SouzSimMerchantCreationService(@Qualifier("souzSimWebClient") WebClient webClient,
                                             SouzSimProperties souzProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig) {
        super(webClient, souzProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SOUZ_SIM;
    }
}
