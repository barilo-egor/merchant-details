package tgb.cryptoexchange.merchantdetails.details.buckspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.BucksPaySimProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class BucksPaySimOrderCreationService extends BucksPayOrderCreationService {


    protected BucksPaySimOrderCreationService(@Qualifier("bucksPaySimWebClient") WebClient webClient,
                                              BucksPaySimProperties bucksPayProperties, CallbackConfig callbackConfig,
                                              SignatureService signatureService) {
        super(webClient, bucksPayProperties, callbackConfig, signatureService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY_SIM;
    }

}
