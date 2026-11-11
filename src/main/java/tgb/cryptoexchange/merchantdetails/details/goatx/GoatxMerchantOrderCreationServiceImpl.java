package tgb.cryptoexchange.merchantdetails.details.goatx;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GoatxPropertiesImpl;

@Service
public class GoatxMerchantOrderCreationServiceImpl extends GoatxMerchantOrderCreationService {

    protected GoatxMerchantOrderCreationServiceImpl(@Qualifier("goatxRestClient") RestClient restClient,
                                                    GoatxPropertiesImpl goatxProperties) {
        super(restClient, goatxProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GOAT_X;
    }
}
