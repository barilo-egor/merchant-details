package tgb.cryptoexchange.merchantdetails.details.paylee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayLeeQRProperties;

@Service
public class PayLeeQRMerchantService extends PayLeeMerchantService {

    protected PayLeeQRMerchantService(@Qualifier("payLeeQRWebClient") WebClient webClient,
                                      PayLeeQRProperties payLeeQRProperties) {
        super(webClient, payLeeQRProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE_QR;
    }
}
