package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.DeoraProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class DeoraMerchantCreationService extends BridgePayOrderCreationService {

    protected DeoraMerchantCreationService(@Qualifier("deoraWebClient") WebClient webClient,
                                           DeoraProperties deoraProperties, SignatureService signatureService,
                                           CallbackConfig callbackConfig) {
        super(webClient, deoraProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.DEORA;
    }
}
