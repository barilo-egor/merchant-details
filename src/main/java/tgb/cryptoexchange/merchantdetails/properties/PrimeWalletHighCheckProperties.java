package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prime-wallet-high-check")
public record PrimeWalletHighCheckProperties(String url, String token, String signKey) implements PayBoxProperties {

}
