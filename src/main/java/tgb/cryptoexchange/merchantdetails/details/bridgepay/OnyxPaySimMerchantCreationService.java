package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPaySimProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class OnyxPaySimMerchantCreationService extends BridgePayOrderCreationService {

    protected OnyxPaySimMerchantCreationService(@Qualifier("onyxPaySimWebClient") WebClient webClient,
                                                OnyxPaySimProperties onyxPaySimProperties,
                                                SignatureService signatureService) {
        super(webClient, onyxPaySimProperties, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONYX_PAY_SIM;
    }
}
