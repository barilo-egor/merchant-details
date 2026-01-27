package tgb.cryptoexchange.merchantdetails.details.studio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.StudioConfig;

@Service
@Slf4j
public class StudioSimOrderCreationService extends StudioOrderCreationService {

    protected StudioSimOrderCreationService(@Qualifier("studioSimWebClient") WebClient webClient,
            @Qualifier("studio-sim-tgb.cryptoexchange.merchantdetails.properties.StudioSimProperties") StudioConfig studioConfig,
            CallbackConfig callbackConfig) {
        super(webClient, studioConfig, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STUDIO_SIM;
    }

}
