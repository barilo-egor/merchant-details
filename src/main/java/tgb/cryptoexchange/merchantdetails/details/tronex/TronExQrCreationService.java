package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.TronExQrProperties;

@Service
public class TronExQrCreationService extends TronExOrderCreationService {


    protected TronExQrCreationService(@Qualifier("tronExRestClient") RestClient restClient,
                                      TronExQrProperties tronExQrProperties, CallbackConfig callbackConfig) {
        super(restClient, tronExQrProperties, callbackConfig);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_QR;
    }

}
