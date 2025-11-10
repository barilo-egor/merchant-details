package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.StormTradeProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class StormTradeMerchantCreationService extends BridgePayOrderCreationService {

    protected StormTradeMerchantCreationService(@Qualifier("stormTradeWebClient") WebClient webClient,
                                                StormTradeProperties stormTradeProperties, SignatureService signatureService) {
        super(webClient, stormTradeProperties, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.STORM_TRADE;
    }
}
