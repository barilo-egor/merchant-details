package tgb.cryptoexchange.merchantdetails.details.noros;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.NorosHighCheckProperties;

@Service
public class NorosHighCheckOrderCreationServiceImpl extends NorosOrderCreationService {

    protected NorosHighCheckOrderCreationServiceImpl(@Qualifier("norosHighCheckWebClient") WebClient webClient,
            NorosHighCheckProperties norosHighCheckProperties) {
        super(webClient, norosHighCheckProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.NOROS_HIGH_CHECK;
    }

}
