package net.rcetech.md.integrations.platform.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.WatPdfProperties;
import net.rcetech.md.integrations.service.ReceiptService;

@Service
public class WatPdfOrderCreationService extends WatOrderCreationService {


    protected WatPdfOrderCreationService(@Qualifier("watRestClient") RestClient restClient,
                                         WatPdfProperties watPdfProperties, CallbackConfig callbackConfig,
                                         ReceiptService receiptService) {
        super(restClient, watPdfProperties, callbackConfig, receiptService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT_PDF;
    }


}
