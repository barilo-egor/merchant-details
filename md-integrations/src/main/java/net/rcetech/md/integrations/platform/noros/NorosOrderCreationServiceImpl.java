package net.rcetech.md.integrations.platform.noros;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.NorosPropertiesImpl;

@Service
public class NorosOrderCreationServiceImpl extends NorosOrderCreationService {

    protected NorosOrderCreationServiceImpl(@Qualifier("norosRestClient") RestClient restClient,
            NorosPropertiesImpl norosProperties) {
        super(restClient, norosProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NOROS;
    }

}
