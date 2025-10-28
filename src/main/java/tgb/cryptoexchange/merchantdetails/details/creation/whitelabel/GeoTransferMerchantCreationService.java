package tgb.cryptoexchange.merchantdetails.details.creation.whitelabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GeoTransferProperties;

@Service
public class GeoTransferMerchantCreationService extends WhiteLabelOrderCreationService {

    protected GeoTransferMerchantCreationService(@Qualifier("geoTransferWebClient") WebClient webClient,
                                                 GeoTransferProperties whiteLabelProperties, ObjectMapper objectMapper) {
        super(webClient, whiteLabelProperties, objectMapper);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GEO_TRANSFER;
    }
}
