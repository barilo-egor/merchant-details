package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.PlataPaymentProperties;

@Service
public class PlataPaymentOrderCreationService extends LevelPayOrderCreationService {

    protected PlataPaymentOrderCreationService(@Qualifier("plataPaymentWebClient") WebClient webClient,
                                               CallbackConfig callbackConfig, PlataPaymentProperties plataPaymentProperties) {
        super(webClient, callbackConfig, plataPaymentProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PLATA_PAYMENT;
    }
}
