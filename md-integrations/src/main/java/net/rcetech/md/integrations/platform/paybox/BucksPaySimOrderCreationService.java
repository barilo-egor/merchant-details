package net.rcetech.md.integrations.platform.paybox;

import net.rcetech.md.integrations.properties.BucksPaySimProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

@Service
public class BucksPaySimOrderCreationService extends PayBoxOrderCreationService {

    protected BucksPaySimOrderCreationService(@Qualifier("bucksPaySimRestClient") RestClient restClient,
                                              BucksPaySimProperties bucksPaySimProperties) {
        super(restClient, bucksPaySimProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY_SIM;
    }
}
