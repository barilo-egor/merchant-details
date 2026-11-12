package net.rcetech.md.integrations.platform.buckspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.BucksPayPropertiesImpl;
import net.rcetech.md.integrations.service.SignatureService;

@Service
public class BucksPayOrderCreationServiceImpl extends BucksPayOrderCreationService {


    protected BucksPayOrderCreationServiceImpl(@Qualifier("bucksPayRestClient") RestClient restClient,
                                               BucksPayPropertiesImpl bucksPayProperties, CallbackConfig callbackConfig,
                                               SignatureService signatureService) {
        super(restClient, bucksPayProperties, callbackConfig, signatureService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.BUCKS_PAY;
    }

}
