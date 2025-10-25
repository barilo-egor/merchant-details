package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alfa-team")
public record AlfaTeamProperties(String url, String token, String key, String secret) {
}
