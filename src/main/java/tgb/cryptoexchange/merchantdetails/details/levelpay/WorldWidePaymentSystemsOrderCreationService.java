package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.WorldWidePaymentSystemsProperties;

@Service
public class WorldWidePaymentSystemsOrderCreationService extends LevelPayOrderCreationService {

    protected WorldWidePaymentSystemsOrderCreationService(@Qualifier("worldWidePaymentsSystemsWebClient") WebClient webClient,
                                                          WorldWidePaymentSystemsProperties worldWidePaymentSystemsProperties) {
        super(webClient, worldWidePaymentSystemsProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.WORLD_WIDE;
    }
}
