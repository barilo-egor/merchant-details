package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "onyx-pay")
public record OnyxPayProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
