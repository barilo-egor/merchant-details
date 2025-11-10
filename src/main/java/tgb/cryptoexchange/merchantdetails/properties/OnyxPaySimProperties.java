package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "onyx-pay-sim")
public record OnyxPaySimProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
