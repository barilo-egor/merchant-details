package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowPropertiesImpl;

@Service
public class PayscrowOrderCreationServiceImpl extends PayscrowOrderCreationService {

    protected PayscrowOrderCreationServiceImpl(@Qualifier("payscrowWebClient") WebClient webClient,
                                               PayscrowPropertiesImpl payscrowProperties) {
        super(webClient, payscrowProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW;
    }
}
