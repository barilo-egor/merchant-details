package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamWTProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class AlfaTeamWTMerchantOrderCreationService extends BridgePayOrderCreationService{

    protected AlfaTeamWTMerchantOrderCreationService(@Qualifier("alfaTeamWTWebClient") WebClient webClient,
                                                     AlfaTeamWTProperties alfaTeamWTProperties,
                                                     SignatureService signatureService, CallbackConfig callbackConfig) {
        super(webClient, alfaTeamWTProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM_WT;
    }
}
