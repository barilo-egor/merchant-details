package net.rcetech.md.integrations.platform.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.PrimeWalletProperties;

@Service
public class PrimeWalletOrderCreationService extends PayBoxOrderCreationService {

    private final PrimeWalletProperties primeWalletProperties;

    protected PrimeWalletOrderCreationService(@Qualifier("primeWalletRestClient") RestClient restClient,
                                              PrimeWalletProperties primeWalletProperties) {
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
        return Merchant.PRIME_WALLET;
    }

}
