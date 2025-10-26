package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "well-bit")
public record WellBitProperties(String url, String secret, String login, String id, String token) {
}
