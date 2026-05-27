package tgb.cryptoexchange.merchantdetails.details.mansory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.MansoryHighCheckProperties;

@Service
@Slf4j
public class MansoryHighCheckService extends MansoryService {

    protected MansoryHighCheckService(@Qualifier("mansoryHighCheckWebClient") WebClient webClient,
                                      MansoryHighCheckProperties mansoryProperties, CallbackConfig callbackConfig) {
        super(webClient, mansoryProperties, callbackConfig);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MANSORY_HIGH_CHECK;
    }

}
