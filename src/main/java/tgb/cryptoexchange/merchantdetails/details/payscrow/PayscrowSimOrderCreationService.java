package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowSimProperties;

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
