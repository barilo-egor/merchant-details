package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PrimeWalletHighCheckProperties;

@Service
public class PrimeWalletHighCheckOrderCreationService extends PayBoxOrderCreationService {

    private final PrimeWalletHighCheckProperties primeWalletProperties;

    protected PrimeWalletHighCheckOrderCreationService(@Qualifier("primeWalletWebClient") WebClient webClient,
                                                       PrimeWalletHighCheckProperties primeWalletProperties) {
        super(webClient, primeWalletProperties);
        this.primeWalletProperties = primeWalletProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + primeWalletProperties.token());
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PRIME_WALLET_HIGH_CHECK;
    }

}
