package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PwPayProperties;

@Service
public class PwPayOrderCreationService extends PayBoxOrderCreationService {

    protected PwPayOrderCreationService(@Qualifier("pwPayRestClient") RestClient restClient,
                                            PwPayProperties pwPayProperties) {
        super(restClient, pwPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PW_PAY;
    }
}
