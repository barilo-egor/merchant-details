package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;
import tgb.cryptoexchange.merchantdetails.properties.PwPayProperties;

@Service
public class PwPayOrderCreationService extends PayBoxOrderCreationService {

    protected PwPayOrderCreationService(@Qualifier("pwPayWebClient") WebClient webClient,
                                            PwPayProperties pwPayProperties) {
        super(webClient, pwPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PW_PAY;
    }
}
