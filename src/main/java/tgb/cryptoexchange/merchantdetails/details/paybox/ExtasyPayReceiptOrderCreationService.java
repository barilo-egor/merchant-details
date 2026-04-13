package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayReceiptProperties;

@Service
@Slf4j
public class ExtasyPayReceiptOrderCreationService extends ReceiptOrderCreationService {

    protected ExtasyPayReceiptOrderCreationService(@Qualifier("extasyPayReceiptWebClient") WebClient webClient,
                                                   ExtasyPayReceiptProperties extasyPayReceiptProperties) {
        super(webClient, extasyPayReceiptProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY_RECEIPT;
    }


}
