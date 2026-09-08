package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowTransgranProperties;

@Service
public class PayscrowTransgranOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowTransgranOrderCreationService(@Qualifier("payscrowTransgranWebClient") WebClient webClient,
                                                    PayscrowTransgranProperties payscrowTransgranProperties) {
        super(webClient, payscrowTransgranProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_TRANSGRAN;
    }
}
