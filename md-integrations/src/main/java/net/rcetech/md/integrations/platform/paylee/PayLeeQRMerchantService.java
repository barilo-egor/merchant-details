package net.rcetech.md.integrations.platform.paylee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PayLeeQRProperties;

@Service
public class PayLeeQRMerchantService extends PayLeeMerchantService {

    protected PayLeeQRMerchantService(@Qualifier("payLeeQRRestClient") RestClient restClient,
                                      PayLeeQRProperties payLeeQRProperties) {
        super(restClient, payLeeQRProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE_QR;
    }
}
