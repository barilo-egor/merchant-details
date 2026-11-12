package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.OnyxPayProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

import java.util.function.Function;

@Service
public class OnyxPayMerchantCreationService extends BridgePayOrderCreationService {

    private final OnyxPayProperties onyxPayProperties;

    protected OnyxPayMerchantCreationService(@Qualifier("onyxPayRestClient") RestClient restClient,
                                             OnyxPayProperties onyxPayProperties, SignatureService signatureService,
                                             CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, onyxPayProperties, signatureService, callbackConfig, sleepingService);
        this.onyxPayProperties = onyxPayProperties;
    }

    @Override
    protected Function<Method, String> keyFunction() {
        return method -> {
            if (Method.MOBILE_TOP_UP.equals(method)) {
                return onyxPayProperties.simKey();
            }
            if (Method.CROSS_BORDER.equals(method)) {
                return onyxPayProperties.tPayKey();
            }
            return onyxPayProperties.key();
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = super.body(detailsRequest);
        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        if (Method.CROSS_BORDER.equals(method)) {
            request.setCrossBorderCurrency("UZS");
        }
        return request;
    }

    @Override
    protected void fillRequisites(DetailsResponse requisiteVO, Response response) {
        super.fillRequisites(requisiteVO, response);

        DealDTO dealDTO = response.getDeals().getFirst();
        if (Method.CROSS_BORDER.equals(dealDTO.getPaymentOption())) {
            requisiteVO.setQr(dealDTO.getQrCodeLink());
            requisiteVO.setDetails(null);
            requisiteVO.setBank(null);
        }
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.ONYX_PAY;
    }
}
