package net.rcetech.md.integrations.platform.buckspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.BucksPaySimProperties;
import net.rcetech.md.integrations.service.SignatureService;

@Service
public class BucksPaySimOrderCreationService extends BucksPayOrderCreationService {


    protected BucksPaySimOrderCreationService(@Qualifier("bucksPaySimRestClient") RestClient restClient,
                                              BucksPaySimProperties bucksPayProperties, CallbackConfig callbackConfig,
                                              SignatureService signatureService) {
        super(restClient, bucksPayProperties, callbackConfig, signatureService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY_SIM;
    }

}
