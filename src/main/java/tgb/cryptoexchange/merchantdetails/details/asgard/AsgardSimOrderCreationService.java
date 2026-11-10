package tgb.cryptoexchange.merchantdetails.details.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AsgardSimProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
@Slf4j
public class AsgardSimOrderCreationService extends AsgardOrderCreationService {

    protected AsgardSimOrderCreationService(@Qualifier("asgardSimRestClient") RestClient restClient,
                                            AsgardSimProperties asgardProperties, SignatureService signatureService,
                                            CallbackConfig callbackConfig) {
        super(restClient, asgardProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ASGARD_SIM;
    }

}
