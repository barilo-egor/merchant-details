package tgb.cryptoexchange.merchantdetails.details.goatx;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GoatxPropertiesImpl;

@Service
public class GoatxMerchantOrderCreationServiceImpl extends GoatxMerchantOrderCreationService {

    protected GoatxMerchantOrderCreationServiceImpl(@Qualifier("goatxWebClient") WebClient webClient,
                                                    GoatxPropertiesImpl goatxProperties) {
        super(webClient, goatxProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GOAT_X;
    }
}
