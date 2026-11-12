package net.rcetech.md.integrations.platform.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.GambitImplProperties;

@Service
@Slf4j
public class GambitImplOrderCreationService extends GambitOrderCreationService {

    protected GambitImplOrderCreationService(@Qualifier("gambitRestClient") RestClient gambitRestClient,
                                             GambitImplProperties gambitProperties) {
        super(gambitRestClient, gambitProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GAMBIT;
    }

}
