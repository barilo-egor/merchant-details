package net.rcetech.md.integrations.platform.bridgepay;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.ExtasyPayPdfProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

/**
 * Техническая часть ExtasyPay PDF чек. Запрос аналогичен SBP/TO_CARD, отличается api-ключом.
 */
@Service
@Slf4j
public class ExtasyPayPdfMerchantOrderCreationService extends BridgePayOrderCreationService {

    protected ExtasyPayPdfMerchantOrderCreationService(@Qualifier("extasyPayPdfRestClient") RestClient restClient,
                                                       ExtasyPayPdfProperties extasyPayPdfProperties,
                                                       SignatureService signatureService,
                                                       CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, extasyPayPdfProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_PDF;
    }
}
