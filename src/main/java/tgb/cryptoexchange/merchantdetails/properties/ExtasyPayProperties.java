package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extasy-pay")
public record ExtasyPayProperties(String url, String token) {
}
