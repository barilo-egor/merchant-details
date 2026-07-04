package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "base51-low-check")
public record Base51LowCheckProperties(String url, String clientId, String clientSecret) implements CrocoPayProperties {
}
