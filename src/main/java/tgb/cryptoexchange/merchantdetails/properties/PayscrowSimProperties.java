package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow-sim")
public record PayscrowSimProperties(String url, String key) implements PayscrowProperties {
}
