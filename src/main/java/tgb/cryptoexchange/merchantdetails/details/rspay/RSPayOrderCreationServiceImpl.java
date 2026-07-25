package tgb.cryptoexchange.merchantdetails.details.rspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.enums.RequiredReceipt;
import tgb.cryptoexchange.merchantdetails.properties.RSPayImplProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

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
        isRequiredReceipt().ifPresent(requiredReceipt -> {
            if (RequiredReceipt.PDF.equals(requiredReceipt)) request.setReceipt(true);
        });
        return request;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.RS_PAY;
    }

}
