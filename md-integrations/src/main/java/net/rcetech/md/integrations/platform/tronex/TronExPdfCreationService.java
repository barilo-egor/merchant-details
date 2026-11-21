package net.rcetech.md.integrations.platform.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.TronExPdfProperties;

@Service
public class TronExPdfCreationService extends TronExOrderCreationService {


    protected TronExPdfCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                       TronExPdfProperties tronExPdfProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExPdfProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_PDF;
    }

}
