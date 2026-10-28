package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.BitBolideWTProperties;

@Service
public class BitBolideWTOrderCreationService extends PayBoxOrderCreationService {

    private final BitBolideWTProperties bitBolideProperties;

    protected BitBolideWTOrderCreationService(@Qualifier("bitBolideWebClient") WebClient webClient,
                                              BitBolideWTProperties bitBolideProperties) {
        super(webClient, bitBolideProperties);
        this.bitBolideProperties = bitBolideProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + bitBolideProperties.token());
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BIT_BOLIDE_WT;
    }

}
