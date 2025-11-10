package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storm-trade")
public record StormTradeProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
