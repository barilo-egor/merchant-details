package tgb.cryptoexchange.merchantdetails.details.payscrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowPropertiesImpl;

import java.util.function.Predicate;

@Service
@Slf4j
public class PayscrowOrderCreationServiceImpl extends PayscrowOrderCreationService {

    private static final int AMOUNT_BOUND = 10_000;

    protected PayscrowOrderCreationServiceImpl(@Qualifier("payscrowWebClient") WebClient webClient,
                                               PayscrowPropertiesImpl payscrowProperties) {
        super(webClient, payscrowProperties);
    }

    @Override
    protected Predicate<DetailsRequest> isValidRequestPredicate() {
        return detailsRequest -> {
            Method method = parseMethod(detailsRequest, Method.class);
            return !Method.TRIANGLE.equals(method) || detailsRequest.getAmount() < AMOUNT_BOUND;
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW;
    }

    @Override
    protected Boolean getUniqueAmount() {
        return true;
    }
}
