package net.rcetech.md.integrations.platform.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayscrowWTProperties;

@Service
public class PayscrowWTOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowWTOrderCreationService(@Qualifier("payscrowWTRestClient") RestClient restClient,
                                             PayscrowWTProperties payscrowWTProperties) {
        super(restClient, payscrowWTProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_WHITE_TRIANGLE;
    }
}
