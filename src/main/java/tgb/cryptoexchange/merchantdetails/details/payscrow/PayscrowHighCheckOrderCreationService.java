package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowHighCheckProperties;

@Service
public class PayscrowHighCheckOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowHighCheckOrderCreationService(@Qualifier("payscrowHighCheckRestClient") RestClient restClient,
                                                    PayscrowHighCheckProperties payscrowHighCheckProperties) {
        super(restClient, payscrowHighCheckProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_HIGH_CHECK;
    }
}
