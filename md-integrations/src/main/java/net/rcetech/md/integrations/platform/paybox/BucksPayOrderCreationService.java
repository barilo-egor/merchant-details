package net.rcetech.md.integrations.platform.paybox;

import net.rcetech.md.integrations.properties.BucksPayProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

@Service
public class BucksPayOrderCreationService extends PayBoxOrderCreationService {

    protected BucksPayOrderCreationService(@Qualifier("bucksPayRestClient") RestClient restClient,
                                           BucksPayProperties bucksPayProperties) {
        super(restClient, bucksPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY;
    }
}
