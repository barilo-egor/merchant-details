package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pay-finity")
public record PayFinityProperties(String url, Keys keys, Integer timeout) {
    public record Keys(String publicKey, String privateKey) {
    }
}
