package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storm-trade13")
public record StormTrade13Properties(String url, String token, String key,
                                     String secret) implements BridgePayProperties {
}
