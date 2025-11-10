package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.function.Function;

@Service
public class OnyxPayMerchantCreationService extends BridgePayOrderCreationService {

    private final OnyxPayProperties onyxPayProperties;

    protected OnyxPayMerchantCreationService(@Qualifier("onyxPayWebClient") WebClient webClient,
                                             OnyxPayProperties onyxPayProperties, SignatureService signatureService) {
        super(webClient, onyxPayProperties, signatureService);
        this.onyxPayProperties = onyxPayProperties;
    }

    @Override
    protected Function<Method, String> keyFunction() {
        return method -> {
            if (Method.MOBILE_TOP_UP.equals(method)) {
                return onyxPayProperties.simKey();
            }
            return onyxPayProperties.key();
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONYX_PAY;
    }
}
