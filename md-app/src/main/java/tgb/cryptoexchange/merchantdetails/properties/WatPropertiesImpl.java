package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wat")
public record WatPropertiesImpl(String url, String token) implements WatProperties {
}
