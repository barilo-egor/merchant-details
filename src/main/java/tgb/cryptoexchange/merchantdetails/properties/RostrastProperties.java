package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rostrast")
public record RostrastProperties(String url, String token, String key, String secret) implements WhiteLabelProperties {
}
