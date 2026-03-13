package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayQRProperties;

@Service
@Slf4j
public class ExtasyPayQROrderCreationService extends PayBoxOrderCreationService {

    protected ExtasyPayQROrderCreationService(@Qualifier("extasyPayQRWebClient") WebClient webClient,
                                              ExtasyPayQRProperties extasyPayQRProperties) {
        super(webClient, extasyPayQRProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_QR;
    }

}
