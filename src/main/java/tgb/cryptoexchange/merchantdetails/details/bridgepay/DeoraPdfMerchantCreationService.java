package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.DeoraPdfProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class DeoraPdfMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraPdfMerchantCreationService(@Qualifier("deoraPdfWebClient") WebClient webClient,
                                              DeoraPdfProperties deoraProperties, SignatureService signatureService,
                                              CallbackConfig callbackConfig) {
        super(webClient, deoraProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA_PDF;
    }
}
