package tgb.cryptoexchange.merchantdetails.details.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AsgardImplProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
@Slf4j
public class AsgardImplOrderCreationService extends AsgardOrderCreationService {

    protected AsgardImplOrderCreationService(@Qualifier("asgardWebClient") WebClient webClient,
                                             AsgardImplProperties asgardProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig) {
        super(webClient, asgardProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ASGARD;
    }

}
