package tgb.cryptoexchange.merchantdetails.details.creation.whitelabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamProperties;

@Service
public class AlfaTeamMerchantCreationService extends WhiteLabelOrderCreationService {

    protected AlfaTeamMerchantCreationService(@Qualifier("alfaTeamWebClient") WebClient webClient,
                                              AlfaTeamProperties alfaTeamProperties, ObjectMapper objectMapper) {
        super(webClient, alfaTeamProperties, objectMapper);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ALFA_TEAM;
    }
}
