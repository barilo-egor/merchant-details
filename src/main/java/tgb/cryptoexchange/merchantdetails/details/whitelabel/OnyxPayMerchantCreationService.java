package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class OnyxPayMerchantCreationService extends WhiteLabelOrderCreationService {

    protected OnyxPayMerchantCreationService(@Qualifier("onyxPayWebClient") WebClient webClient,
                                             OnyxPayProperties onyxPayProperties, SignatureService signatureService) {
        super(webClient, onyxPayProperties, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONYX_PAY;
    }
}
