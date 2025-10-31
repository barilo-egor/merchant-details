package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ParadoxPayProperties;

@Service
public class ParadoxPayOrderCreationService extends LevelPayOrderCreationService {

    protected ParadoxPayOrderCreationService(@Qualifier("paradoxPayWebClient") WebClient webClient,
                                             ParadoxPayProperties paradoxPayProperties) {
        super(webClient, paradoxPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PARADOX_PAY;
    }
}
