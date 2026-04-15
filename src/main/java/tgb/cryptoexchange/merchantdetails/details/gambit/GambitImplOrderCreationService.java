package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GambitImplProperties;

@Service
@Slf4j
public class GambitImplOrderCreationService extends GambitOrderCreationService {

    protected GambitImplOrderCreationService(@Qualifier("gambitWebClient") WebClient gambitWebClient,
                                             GambitImplProperties gambitProperties) {
        super(gambitWebClient, gambitProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GAMBIT;
    }

}
