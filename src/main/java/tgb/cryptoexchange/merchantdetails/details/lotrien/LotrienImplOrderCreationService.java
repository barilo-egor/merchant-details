package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

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
