package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tron-ex-dl")
public record TronExDLProperties(String url, String dlUrl, String key) implements TronExProperties {
}
