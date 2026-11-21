package net.rcetech.md.integrations.platform.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayscrowLowProperties;

@Service
public class PayscrowLowOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowLowOrderCreationService(@Qualifier("payscrowLowRestClient") RestClient restClient,
                                              PayscrowLowProperties payscrowLowProperties) {
        super(restClient, payscrowLowProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_LOW;
    }
}
