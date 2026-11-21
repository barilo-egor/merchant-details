package net.rcetech.md.integrations.platform.bridgepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.GeoTransferProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;

import java.util.function.Function;

@Service
public class GeoTransferMerchantCreationService extends BridgePayOrderCreationService {

    private final GeoTransferProperties geoTransferProperties;

    protected GeoTransferMerchantCreationService(@Qualifier("geoTransferRestClient") RestClient restClient,
                                                 GeoTransferProperties geoTransferProperties, SignatureService signatureService,
                                                 CallbackConfig callbackConfig, SleepingService sleepingService) {
        super(restClient, geoTransferProperties, signatureService, callbackConfig, sleepingService);
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

