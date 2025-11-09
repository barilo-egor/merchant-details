package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow")
public record PayscrowPropertiesImpl(String url, String key) implements PayscrowProperties {
}
