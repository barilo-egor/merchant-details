package net.rcetech.md.integrations.platform.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.LotrienProperties;

@Service
@Slf4j
public class LotrienImplOrderCreationService extends LotrienOrderCreationService {

    protected LotrienImplOrderCreationService(@Qualifier("lotrienRestClient") RestClient restClient,
                                              LotrienProperties lotrienProperties) {
        super(restClient, lotrienProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.LOTRIEN;
    }

}
