package tgb.cryptoexchange.merchantdetails.details.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.WatPdfProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;

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
