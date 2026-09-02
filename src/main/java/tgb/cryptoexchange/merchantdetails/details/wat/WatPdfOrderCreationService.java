package tgb.cryptoexchange.merchantdetails.details.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.WatPdfProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;

@Service
public class WatPdfOrderCreationService extends WatOrderCreationService {


    protected WatPdfOrderCreationService(@Qualifier("watWebClient") WebClient webClient,
                                         WatPdfProperties watPdfProperties, CallbackConfig callbackConfig,
                                         ReceiptService receiptService) {
        super(webClient, watPdfProperties, callbackConfig, receiptService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT_PDF;
    }


}
