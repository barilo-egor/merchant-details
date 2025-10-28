package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.RostrastProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class RostrastMerchantCreationService extends WhiteLabelOrderCreationService {

    protected RostrastMerchantCreationService(@Qualifier("rostrastWebClient") WebClient webClient,
                                              RostrastProperties whiteLabelProperties, ObjectMapper objectMapper,
                                              SignatureService signatureService) {
        super(webClient, whiteLabelProperties, objectMapper, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ROSTRAST;
    }
}
