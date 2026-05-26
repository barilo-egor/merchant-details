package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mansory")
public record MansoryImplProperties(String url, String apiKey, String secret) implements MansoryProperties {
}
