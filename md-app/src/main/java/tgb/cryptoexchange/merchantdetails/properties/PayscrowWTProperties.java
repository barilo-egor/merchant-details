package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow-white-triangle")
public record PayscrowWTProperties(String url, String key) implements PayscrowProperties {
}
