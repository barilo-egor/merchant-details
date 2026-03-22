package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "viatrum")
public record ViatrumProperties(String url, String pub, String secret, String environment) {
}
