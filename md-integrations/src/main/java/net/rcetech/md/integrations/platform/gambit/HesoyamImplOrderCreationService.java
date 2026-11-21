package net.rcetech.md.integrations.platform.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.HesoyamImplProperties;

@Service
@Slf4j
public class HesoyamImplOrderCreationService extends GambitOrderCreationService {

    protected HesoyamImplOrderCreationService(@Qualifier("hesoyamRestClient") RestClient hesoyamRestClient,
                                              HesoyamImplProperties hesoyamProperties) {
        super(hesoyamRestClient, hesoyamProperties);
    }

    protected HesoyamMethod parseDetailsMethod(String method) {
        return parseMethod(method, HesoyamMethod.class);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HESOYAM;
    }

}
