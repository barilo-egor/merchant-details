package net.rcetech.md.integrations.platform.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayscrowSimProperties;

@Service
public class PayscrowSimOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowSimOrderCreationService(@Qualifier("payscrowSimRestClient") RestClient restClient,
                                              PayscrowSimProperties payscrowSimProperties) {
        super(restClient, payscrowSimProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_SIM;
    }
}
