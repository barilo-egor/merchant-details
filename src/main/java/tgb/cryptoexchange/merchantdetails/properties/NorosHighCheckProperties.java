package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "noros-high-check")
public record NorosHighCheckProperties(String url, String key) implements NorosProperties {
}
