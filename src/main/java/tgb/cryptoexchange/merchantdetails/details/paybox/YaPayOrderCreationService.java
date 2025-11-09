package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.YaPayProperties;

@Service
public class YaPayOrderCreationService extends PayBoxOrderCreationService {

    protected YaPayOrderCreationService(@Qualifier("yaPayWebClient") WebClient webClient,
                                        YaPayProperties yaPayProperties) {
        super(webClient, yaPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.YA_PAY;
    }
}
