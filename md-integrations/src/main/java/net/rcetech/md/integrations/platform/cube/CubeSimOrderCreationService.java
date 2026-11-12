package net.rcetech.md.integrations.platform.cube;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.CubeSimProperties;

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
