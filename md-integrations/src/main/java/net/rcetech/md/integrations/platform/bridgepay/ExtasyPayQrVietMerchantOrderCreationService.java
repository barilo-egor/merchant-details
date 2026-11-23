package net.rcetech.md.integrations.platform.bridgepay;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.ExtasyPayQrVietProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

/**
 * Техническая часть ExtasyPay QR Vietnam.
 */
@Service
@Slf4j
public class ExtasyPayQrVietMerchantOrderCreationService extends BridgePayOrderCreationService {

    protected ExtasyPayQrVietMerchantOrderCreationService(@Qualifier("extasyPayQrVietRestClient") RestClient restClient,
                                                          ExtasyPayQrVietProperties extasyPayQrVietProperties,
                                                          SignatureService signatureService,
                                                          CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, extasyPayQrVietProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_QR_VIET;
    }
}
