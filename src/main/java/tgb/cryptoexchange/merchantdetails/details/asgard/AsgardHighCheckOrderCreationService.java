package tgb.cryptoexchange.merchantdetails.details.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AsgardHighCheckProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
@Slf4j
public class AsgardHighCheckOrderCreationService extends AsgardOrderCreationService {

    protected AsgardHighCheckOrderCreationService(@Qualifier("asgardHighCheckWebClient") WebClient webClient,
                                                  AsgardHighCheckProperties asgardProperties,
                                                  SignatureService signatureService,
                                                  CallbackConfig callbackConfig) {
        super(webClient, asgardProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ASGARD_HIGH_CHECK;
    }

}
