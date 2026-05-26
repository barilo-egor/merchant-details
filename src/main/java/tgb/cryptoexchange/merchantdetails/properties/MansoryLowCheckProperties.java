package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mansory-low-check")
public record MansoryLowCheckProperties(String url, String apiKey, String secret) implements MansoryProperties {
}
