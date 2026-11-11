package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

@Service
public class AlfaTeamMerchantCreationService extends BridgePayOrderCreationService {

    protected AlfaTeamMerchantCreationService(@Qualifier("alfaTeamRestClient") RestClient restClient,
                                              AlfaTeamProperties alfaTeamProperties, SignatureService signatureService,
                                              CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, alfaTeamProperties, signatureService, callbackConfig, sleepingService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM;
    }
}
