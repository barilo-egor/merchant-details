package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AuroraPayProperties;

@Service
public class AuroraPayOrderCreationService extends LevelPayOrderCreationService {

    protected AuroraPayOrderCreationService(@Qualifier("auroraWebClient") WebClient webClient, CallbackConfig callbackConfig,
                                            AuroraPayProperties levelPayProperties) {
        super(webClient, callbackConfig, levelPayProperties);
    }

    @Override
    protected Integer getAmount(String amount) {
        return super.getAmount(amount) / 100;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.AURORA_PAY;
    }
}
