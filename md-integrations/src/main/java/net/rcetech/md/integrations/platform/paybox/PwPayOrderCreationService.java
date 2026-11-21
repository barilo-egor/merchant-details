package net.rcetech.md.integrations.platform.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PwPayProperties;

@Service
public class PwPayOrderCreationService extends PayBoxOrderCreationService {

    protected PwPayOrderCreationService(@Qualifier("pwPayRestClient") RestClient restClient,
                                            PwPayProperties pwPayProperties) {
        super(restClient, pwPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PW_PAY;
    }
}
