package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowSimProperties;

@Service
public class PayscrowSimOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowSimOrderCreationService(@Qualifier("payscrowSimWebClient") WebClient webClient,
                                              PayscrowSimProperties payscrowSimProperties) {
        super(webClient, payscrowSimProperties);
    }

    @Override
    protected Boolean getUniqueAmount() {
        return true;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_SIM;
    }
}
