package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deora-sim")
public record DeoraSimProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
