package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tron-ex-sim")
public record TronExSimProperties(String url, String key) implements TronExProperties {
}
