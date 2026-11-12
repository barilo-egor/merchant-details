package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("croco-pay")
public record CrocoPayImplProperties(String url, String clientId, String clientSecret) implements CrocoPayProperties {
}
