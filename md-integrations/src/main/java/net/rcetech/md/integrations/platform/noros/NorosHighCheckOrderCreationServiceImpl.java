package net.rcetech.md.integrations.platform.noros;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.NorosHighCheckProperties;

@Service
public class NorosHighCheckOrderCreationServiceImpl extends NorosOrderCreationService {

    protected NorosHighCheckOrderCreationServiceImpl(@Qualifier("norosHighCheckRestClient") RestClient restClient,
            NorosHighCheckProperties norosHighCheckProperties) {
        super(restClient, norosHighCheckProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NOROS_HIGH_CHECK;
    }

}
