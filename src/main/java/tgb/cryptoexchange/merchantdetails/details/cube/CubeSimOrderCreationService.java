package tgb.cryptoexchange.merchantdetails.details.cube;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.CubeSimProperties;

@Service
@Slf4j
public class CubeSimOrderCreationService extends CubeOrderCreationService {

    protected CubeSimOrderCreationService(@Qualifier("cubeRestClient") RestClient cubeRestClient,
                                          CubeSimProperties cubeProperties, CallbackConfig callbackConfig) {
        super(cubeRestClient, cubeProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CUBE_SIM;
    }

}
