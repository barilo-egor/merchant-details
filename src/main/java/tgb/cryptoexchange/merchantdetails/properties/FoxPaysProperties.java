package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fox-pays")
public record FoxPaysProperties(String url, String token, String merchantId) {
}
