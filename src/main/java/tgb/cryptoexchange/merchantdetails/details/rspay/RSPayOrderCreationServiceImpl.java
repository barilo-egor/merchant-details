package tgb.cryptoexchange.merchantdetails.details.rspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.RSPayImplProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.Arrays;
import java.util.Optional;

@Service
public class RSPayOrderCreationServiceImpl extends RSPayOrderCreationService {


    protected RSPayOrderCreationServiceImpl(@Qualifier("rsPayWebClient") WebClient webClient,
                                            RSPayImplProperties rsPayProperties, CallbackConfig callbackConfig,
                                            SignatureService signatureService) {
        super(webClient, rsPayProperties, callbackConfig, signatureService);
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = super.body(detailsRequest);
        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        if (Arrays.asList(Method.CARD, Method.SBP).contains(method)) {
            request.setReceipt(true);
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Optional<DetailsResponse> detailsResponseMaybe = super.buildResponse(response);
        if (detailsResponseMaybe.isPresent()) {
            DetailsResponse detailsResponse = detailsResponseMaybe.get();
            if (Arrays.asList(Method.CARD, Method.SBP).contains(response.getPaymentMethod())) {
                detailsResponse.setExternalReceiptDemand(true);
            }
        }
        return detailsResponseMaybe;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.RS_PAY;
    }

}
