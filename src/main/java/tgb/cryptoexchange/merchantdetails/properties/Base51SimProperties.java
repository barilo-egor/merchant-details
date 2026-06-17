package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "base51-sim")
public record Base51SimProperties(String url, String clientId, String clientSecret) implements CrocoPayProperties {
}
