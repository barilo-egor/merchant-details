package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowHighCheckProperties;

@Service
public class PayscrowHighCheckOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowHighCheckOrderCreationService(@Qualifier("payscrowHighCheckWebClient") WebClient webClient,
                                                    PayscrowHighCheckProperties payscrowHighCheckProperties) {
        super(webClient, payscrowHighCheckProperties);
    }

    @Override
    protected Boolean getUniqueAmount() {
        return true;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_HIGH_CHECK;
    }
}
