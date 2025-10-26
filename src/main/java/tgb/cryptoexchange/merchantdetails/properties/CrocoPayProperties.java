package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("croco-pay")
public record CrocoPayProperties(String url, String clientId, String clientSecret) {
}
