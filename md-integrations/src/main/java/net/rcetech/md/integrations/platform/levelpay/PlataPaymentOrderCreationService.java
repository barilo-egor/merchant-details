package net.rcetech.md.integrations.platform.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.PlataPaymentProperties;

@Service
public class PlataPaymentOrderCreationService extends LevelPayOrderCreationService {

    protected PlataPaymentOrderCreationService(@Qualifier("plataPaymentRestClient") RestClient restClient,
                                               CallbackConfig callbackConfig, PlataPaymentProperties plataPaymentProperties) {
        super(restClient, callbackConfig, plataPaymentProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PLATA_PAYMENT;
    }
}
