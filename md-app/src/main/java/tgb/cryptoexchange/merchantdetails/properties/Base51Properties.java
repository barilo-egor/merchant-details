package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "base51")
public record Base51Properties(String url, String clientId, String clientSecret) implements CrocoPayProperties {
}
