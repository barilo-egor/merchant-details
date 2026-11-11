package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "base51-high-check")
public record Base51HighCheckProperties(String url, String clientId,
                                        String clientSecret) implements CrocoPayProperties {
}
