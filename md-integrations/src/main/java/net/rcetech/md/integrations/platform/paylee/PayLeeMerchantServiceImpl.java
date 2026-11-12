package net.rcetech.md.integrations.platform.paylee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayLeePropertiesImpl;

@Service
public class PayLeeMerchantServiceImpl extends PayLeeMerchantService{

    protected PayLeeMerchantServiceImpl(@Qualifier("payLeeRestClient") RestClient restClient,
                                        PayLeePropertiesImpl payLeeProperties) {
        super(restClient, payLeeProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE;
    }
}
