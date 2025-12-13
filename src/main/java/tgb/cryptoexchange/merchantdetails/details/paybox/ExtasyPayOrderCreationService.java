package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;

@Service
public class ExtasyPayOrderCreationService extends PayBoxOrderCreationService {

    protected ExtasyPayOrderCreationService(@Qualifier("extasyPayWebClient") WebClient webClient,
                                            ExtasyPayProperties extasyPayProperties) {
        super(webClient, extasyPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY;
    }
}
