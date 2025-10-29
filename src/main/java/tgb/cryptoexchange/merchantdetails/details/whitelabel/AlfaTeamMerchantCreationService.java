package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class AlfaTeamMerchantCreationService extends WhiteLabelOrderCreationService {

    protected AlfaTeamMerchantCreationService(@Qualifier("alfaTeamWebClient") WebClient webClient,
                                              AlfaTeamProperties alfaTeamProperties, SignatureService signatureService) {
        super(webClient, alfaTeamProperties, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM;
    }
}
