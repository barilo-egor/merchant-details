package tgb.cryptoexchange.merchantdetails.details.wat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.WatSimProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;

@Service
public class WatSimOrderCreationService extends WatOrderCreationService {


    protected WatSimOrderCreationService(@Qualifier("watRestClient") RestClient restClient,
                                         WatSimProperties watSimProperties, CallbackConfig callbackConfig,
                                         ReceiptService receiptService) {
        super(restClient, watSimProperties, callbackConfig, receiptService);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.WAT_SIM;
    }


}
