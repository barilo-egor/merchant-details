package tgb.cryptoexchange.merchantdetails.details.noros;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.NorosPropertiesImpl;

@Service
public class NorosOrderCreationServiceImpl extends NorosOrderCreationService {

    protected NorosOrderCreationServiceImpl(@Qualifier("norosWebClient") WebClient webClient,
            NorosPropertiesImpl norosProperties) {
        super(webClient, norosProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NOROS;
    }

}
