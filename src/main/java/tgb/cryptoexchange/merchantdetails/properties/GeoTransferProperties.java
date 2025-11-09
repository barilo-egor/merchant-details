package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geo-transfer")
public record GeoTransferProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
