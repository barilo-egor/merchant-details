package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowPropertiesImpl;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class PayscrowOrderCreationServiceImpl extends PayscrowOrderCreationService {

    private static final int AMOUNT_BOUND = 10_000;

    private static final  Set<Method> IN_HOUSE_METHODS = Set.of(
            Method.ALFA, Method.PSB, Method.GAZ_PROM, Method.OZON
    );

    private final PayscrowPropertiesImpl payscrowProperties;

    protected PayscrowOrderCreationServiceImpl(@Qualifier("payscrowWebClient") WebClient webClient,
                                               PayscrowPropertiesImpl payscrowProperties) {
        super(webClient, payscrowProperties);
        this.payscrowProperties = payscrowProperties;
    }

    @Override
    protected Predicate<DetailsRequest> isValidRequestPredicate() {
        return detailsRequest -> {
            Method method = parseMethod(detailsRequest.getMethod(), Method.class);
            return !Method.TRIANGLE.equals(method) || detailsRequest.getAmount() < AMOUNT_BOUND;
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW;
    }

    @Override
    public Function<DetailsRequest, String> keyFunction() {
        return detailsRequest -> {
            Method method = parseMethod(detailsRequest.getMethod(), Method.class);
            if (IN_HOUSE_METHODS.contains(method)) {
                return payscrowProperties.inHouseKey();
            }
            if (Method.TRIANGLE.equals(method)) {
                return payscrowProperties.whiteTriangleKey();
            }
            if (detailsRequest.getAmount() < AMOUNT_BOUND) {
                return payscrowProperties.key();
            } else {
                return payscrowProperties.highCheckKey();
            }
        };
    }
}
