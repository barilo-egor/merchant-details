package tgb.cryptoexchange.merchantdetails.details.buckspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.BucksPayHighCheckProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class BucksPayHighCheckOrderCreationService extends BucksPayOrderCreationService {


    protected BucksPayHighCheckOrderCreationService(@Qualifier("bucksPayHighCheckWebClient") WebClient webClient,
                                                    BucksPayHighCheckProperties bucksPayProperties, CallbackConfig callbackConfig,
                                                    SignatureService signatureService) {
        super(webClient, bucksPayProperties, callbackConfig, signatureService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY_HIGH_CHECK;
    }

}
