package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.EclipseGateProperties;

@Service
@Slf4j
public class EclipseGateImplService extends EclipseGateService {

    protected EclipseGateImplService(@Qualifier("eclipseGateWebClient") WebClient webClient,
                                     EclipseGateProperties eclipseGateProperties, CallbackConfig callbackConfig) {
        super(webClient, eclipseGateProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ECLIPSE_GATE;
    }

}
