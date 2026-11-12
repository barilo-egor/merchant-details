package net.rcetech.md.integrations.platform.goatx;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.GoatxSimProperties;

@Service
public class GoatxSimMerchantOrderCreationService extends GoatxMerchantOrderCreationService {

    protected GoatxSimMerchantOrderCreationService(@Qualifier("goatxSimRestClient") RestClient restClient,
                                                   GoatxSimProperties goatxProperties) {
        super(restClient, goatxProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GOAT_X_SIM;
    }
}
