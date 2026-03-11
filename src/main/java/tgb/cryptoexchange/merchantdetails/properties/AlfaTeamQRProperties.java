package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("alfa-team-qr")
public record AlfaTeamQRProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
