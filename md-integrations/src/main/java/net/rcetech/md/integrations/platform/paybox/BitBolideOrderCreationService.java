package net.rcetech.md.integrations.platform.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.BitBolideProperties;

@Service
public class BitBolideOrderCreationService extends PayBoxOrderCreationService {

    private final BitBolideProperties bitBolideProperties;

    protected BitBolideOrderCreationService(@Qualifier("bitBolideRestClient") RestClient restClient,
                                            BitBolideProperties bitBolideProperties) {
        super(restClient, bitBolideProperties);
        this.bitBolideProperties = bitBolideProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + bitBolideProperties.token());
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BIT_BOLIDE;
    }

}
