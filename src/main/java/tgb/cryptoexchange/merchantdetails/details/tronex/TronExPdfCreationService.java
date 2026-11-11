package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.TronExPdfProperties;

@Service
public class TronExPdfCreationService extends TronExOrderCreationService {


    protected TronExPdfCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                       TronExPdfProperties tronExPdfProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExPdfProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_PDF;
    }

}
