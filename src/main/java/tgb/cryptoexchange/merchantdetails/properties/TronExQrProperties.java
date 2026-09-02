package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tron-ex-qr")
public record TronExQrProperties(String url, String key) implements TronExProperties {
}
