package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowWTProperties;

@Service
public class PayscrowWTOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowWTOrderCreationService(@Qualifier("payscrowWTWebClient") WebClient webClient,
                                             PayscrowWTProperties payscrowWTProperties) {
        super(webClient, payscrowWTProperties);
    }

    @Override
    protected Boolean getUniqueAmount() {
        return true;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_WHITE_TRIANGLE;
    }
}
