package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "souz-sbp-qr")
public record SouzSbpQrProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
