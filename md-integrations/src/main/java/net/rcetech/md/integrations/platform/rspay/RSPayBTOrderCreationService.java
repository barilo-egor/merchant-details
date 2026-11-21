package net.rcetech.md.integrations.platform.rspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.RSPayBTProperties;
import net.rcetech.md.integrations.service.SignatureService;

@Service
public class RSPayBTOrderCreationService extends RSPayOrderCreationService {


    protected RSPayBTOrderCreationService(@Qualifier("rsPayRestClient") RestClient restClient,
                                          RSPayBTProperties rsPayProperties, CallbackConfig callbackConfig,
                                          SignatureService signatureService) {
        super(restClient, rsPayProperties, callbackConfig, signatureService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.RS_PAY_BT;
    }

}
