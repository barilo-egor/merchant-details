package net.rcetech.md.integrations.platform.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.Plata18Properties;

@Service
public class Plata18OrderCreationService extends LevelPayOrderCreationService {

    protected Plata18OrderCreationService(@Qualifier("plata18RestClient") RestClient restClient,
                                               CallbackConfig callbackConfig, Plata18Properties plata18Properties) {
        super(restClient, callbackConfig, plata18Properties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PLATA_18;
    }
}
