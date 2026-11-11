package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prime-wallet")
public record PrimeWalletProperties(String url, String token, String signKey) implements PayBoxProperties {

}
