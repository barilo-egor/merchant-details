package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HesoyamImplProperties;

@Service
@Slf4j
public class HesoyamImplOrderCreationService extends GambitOrderCreationService {

    protected HesoyamImplOrderCreationService(@Qualifier("hesoyamWebClient") WebClient hesoyamWebClient,
                                              HesoyamImplProperties hesoyamProperties) {
        super(hesoyamWebClient, hesoyamProperties);
    }

    protected HesoyamMethod parseDetailsMethod(String method) {
        return parseMethod(method, HesoyamMethod.class);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HESOYAM;
    }

}
