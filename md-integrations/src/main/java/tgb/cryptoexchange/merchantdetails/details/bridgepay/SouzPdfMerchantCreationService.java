package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.SouzPdfProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class SouzPdfMerchantCreationService extends BridgePayOrderCreationService {

    protected SouzPdfMerchantCreationService(@Qualifier("souzPdfRestClient") RestClient restClient,
                                             SouzPdfProperties souzProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, souzProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.SOUZ_PDF;
    }
}
