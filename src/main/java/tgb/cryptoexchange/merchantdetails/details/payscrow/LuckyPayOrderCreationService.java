package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.LuckyPayProperties;

@Service
public class LuckyPayOrderCreationService extends PayscrowOrderCreationService {

    protected LuckyPayOrderCreationService(@Qualifier("luckyPayWebClient") WebClient webClient,
                                           LuckyPayProperties luckyPayProperties) {
        super(webClient, luckyPayProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.LUCKY_PAY;
    }

    @Override
    protected Boolean getUniqueAmount() {
        return null;
    }
}
