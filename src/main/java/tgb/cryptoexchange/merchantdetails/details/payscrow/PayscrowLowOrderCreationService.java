package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowLowProperties;

@Service
public class PayscrowLowOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowLowOrderCreationService(@Qualifier("payscrowLowWebClient") WebClient webClient,
                                              PayscrowLowProperties payscrowLowProperties) {
        super(webClient, payscrowLowProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_LOW;
    }
}
