package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamQRProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class AlfaTeamQRMerchantOrderCreationService extends BridgePayOrderCreationService {

    protected AlfaTeamQRMerchantOrderCreationService(@Qualifier("alfaTeamQRWebClient") WebClient webClient,
                                                     AlfaTeamQRProperties alfaTeamQRProperties,
                                                     SignatureService signatureService, CallbackConfig callbackConfig) {
        super(webClient, alfaTeamQRProperties, signatureService, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM_QR;
    }
}
