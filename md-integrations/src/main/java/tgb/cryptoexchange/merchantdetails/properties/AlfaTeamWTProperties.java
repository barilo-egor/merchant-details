package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("alfa-team-white-triangle")
public record AlfaTeamWTProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
