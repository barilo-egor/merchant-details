package net.rcetech.md.integrations.platform.asgard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.AsgardSimProperties;
import net.rcetech.md.integrations.service.SignatureService;

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
