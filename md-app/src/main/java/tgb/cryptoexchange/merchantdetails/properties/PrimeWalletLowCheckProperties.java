package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prime-wallet-low-check")
public record PrimeWalletLowCheckProperties(String url, String token, String signKey) implements PayBoxProperties {

}
