package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "noros")
public record NorosPropertiesImpl(String url, String key) implements NorosProperties {
}
