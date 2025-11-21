package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.RostrastProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class RostrastMerchantCreationService extends BridgePayOrderCreationService {

    protected RostrastMerchantCreationService(@Qualifier("rostrastWebClient") WebClient webClient,
                                              RostrastProperties rostrastProperties, SignatureService signatureService,
                                              CallbackConfig callbackConfig) {
        super(webClient, rostrastProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ROSTRAST;
    }
}
