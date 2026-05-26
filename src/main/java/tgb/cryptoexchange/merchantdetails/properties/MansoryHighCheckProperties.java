package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mansory-high-check")
public record MansoryHighCheckProperties(String url, String apiKey, String secret) implements MansoryProperties {
}
