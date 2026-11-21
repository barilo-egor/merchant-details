package net.rcetech.md.integrations.platform.rspay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.enums.RequiredReceipt;
import net.rcetech.md.integrations.properties.RSPayImplProperties;
import net.rcetech.md.integrations.service.SignatureService;

@Service
public class RSPayOrderCreationServiceImpl extends RSPayOrderCreationService {


    protected RSPayOrderCreationServiceImpl(@Qualifier("rsPayRestClient") RestClient restClient,
                                            RSPayImplProperties rsPayProperties, CallbackConfig callbackConfig,
                                            SignatureService signatureService) {
        super(restClient, rsPayProperties, callbackConfig, signatureService);
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = super.body(detailsRequest);
        if (RequiredReceipt.REQUIRED_RECEIPTS.contains(detailsRequest.getRequiredReceipt())) request.setReceipt(true);
        return request;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.RS_PAY;
    }

}
