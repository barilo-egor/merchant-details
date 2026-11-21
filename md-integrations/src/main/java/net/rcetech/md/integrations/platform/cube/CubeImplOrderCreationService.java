package net.rcetech.md.integrations.platform.cube;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.CubePropertiesImpl;

@Service
@Slf4j
public class CubeImplOrderCreationService extends CubeOrderCreationService {

    protected CubeImplOrderCreationService(@Qualifier("cubeRestClient") RestClient cubeRestClient,
                                           CubePropertiesImpl cubeProperties, CallbackConfig callbackConfig) {
        super(cubeRestClient, cubeProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.CUBE;
    }

}
