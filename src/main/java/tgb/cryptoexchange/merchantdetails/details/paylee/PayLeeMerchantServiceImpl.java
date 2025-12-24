package tgb.cryptoexchange.merchantdetails.details.paylee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayLeePropertiesImpl;

@Service
public class PayLeeMerchantServiceImpl extends PayLeeMerchantService{

    protected PayLeeMerchantServiceImpl(@Qualifier("payLeeWebClient") WebClient webClient,
                                        PayLeePropertiesImpl payLeeProperties) {
        super(webClient, payLeeProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE;
    }
}
