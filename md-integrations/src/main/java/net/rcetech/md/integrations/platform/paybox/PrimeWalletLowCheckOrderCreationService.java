package net.rcetech.md.integrations.platform.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PrimeWalletLowCheckProperties;

@Service
public class PrimeWalletLowCheckOrderCreationService extends PayBoxOrderCreationService {

    private final PrimeWalletLowCheckProperties primeWalletProperties;

    protected PrimeWalletLowCheckOrderCreationService(@Qualifier("primeWalletRestClient") RestClient restClient,
                                                      PrimeWalletLowCheckProperties primeWalletProperties) {
        super(restClient, primeWalletProperties);
        this.primeWalletProperties = primeWalletProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + primeWalletProperties.token());
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PRIME_WALLET_LOW_CHECK;
    }

}
