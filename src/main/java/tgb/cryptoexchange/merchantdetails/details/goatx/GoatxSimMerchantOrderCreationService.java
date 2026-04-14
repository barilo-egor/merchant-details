package tgb.cryptoexchange.merchantdetails.details.goatx;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GoatxSimProperties;

@Service
public class GoatxSimMerchantOrderCreationService extends GoatxMerchantOrderCreationService {

    protected GoatxSimMerchantOrderCreationService(@Qualifier("goatxSimWebClient") WebClient webClient,
                                                   GoatxSimProperties goatxProperties) {
        super(webClient, goatxProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GOAT_X_SIM;
    }
}
