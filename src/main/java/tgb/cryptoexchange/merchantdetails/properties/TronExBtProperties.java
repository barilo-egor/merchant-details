package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tron-ex-bt")
public record TronExBtProperties(String url, String key) implements TronExProperties {
}
