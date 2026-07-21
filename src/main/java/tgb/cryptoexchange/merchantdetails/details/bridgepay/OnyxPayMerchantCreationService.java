package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

import java.util.function.Function;

@Service
public class OnyxPayMerchantCreationService extends BridgePayOrderCreationService {

    private final OnyxPayProperties onyxPayProperties;

    protected OnyxPayMerchantCreationService(@Qualifier("onyxPayWebClient") WebClient webClient,
                                             OnyxPayProperties onyxPayProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(webClient, onyxPayProperties, signatureService, callbackConfig, sleepingService);
        this.onyxPayProperties = onyxPayProperties;
    }

    @Override
    protected Function<Method, String> keyFunction() {
        return method -> {
            if (Method.MOBILE_TOP_UP.equals(method)) {
                return onyxPayProperties.simKey();
            }
            if (Method.T_PAY.equals(method)) {
                return onyxPayProperties.tPayKey();
            }
            return onyxPayProperties.key();
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = super.body(detailsRequest);
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        if (Method.T_PAY.equals(method)) {
            request.setPaymentMethod("tinkoff");
        }
        return request;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONYX_PAY;
    }
}
