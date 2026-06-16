package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.properties.GeoTransferProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.function.Function;

@Service
public class GeoTransferMerchantCreationService extends BridgePayOrderCreationService {

    private final GeoTransferProperties geoTransferProperties;

    protected GeoTransferMerchantCreationService(@Qualifier("geoTransferWebClient") WebClient webClient,
                                                 GeoTransferProperties geoTransferProperties, SignatureService signatureService,
                                                 CallbackConfig callbackConfig) {
        super(webClient, geoTransferProperties, signatureService, callbackConfig);
        this.geoTransferProperties = geoTransferProperties;
    }

    @Override
    protected Function<Method, String> keyFunction() {
        return method -> {
            if (Method.MANUAL_SBP_QR.equals(method)) {
                return geoTransferProperties.keyQr();
            }
            return geoTransferProperties.key();
        };
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GEO_TRANSFER;
    }
}

