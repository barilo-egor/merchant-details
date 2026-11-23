package net.rcetech.md.integrations.platform.bridgepay;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.ExtasyPayProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

/**
 * Техническая часть ExtasyPay.
 */
@Service
@Slf4j
public class ExtasyPayMerchantOrderCreationService extends BridgePayOrderCreationService {

    protected ExtasyPayMerchantOrderCreationService(@Qualifier("extasyPayRestClient") RestClient restClient,
                                                    ExtasyPayProperties extasyPayProperties,
                                                    SignatureService signatureService,
                                                    CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, extasyPayProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY;
    }
}
