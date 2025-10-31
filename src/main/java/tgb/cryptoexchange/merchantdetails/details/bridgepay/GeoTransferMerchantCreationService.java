package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GeoTransferProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

@Service
public class GeoTransferMerchantCreationService extends BridgePayOrderCreationService {

    protected GeoTransferMerchantCreationService(@Qualifier("geoTransferWebClient") WebClient webClient,
                                                 GeoTransferProperties geoTransferProperties, SignatureService signatureService) {
        super(webClient, geoTransferProperties, signatureService);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GEO_TRANSFER;
    }
}

