package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow-low")
public record PayscrowLowProperties(String url, String key) implements PayscrowProperties {
}
