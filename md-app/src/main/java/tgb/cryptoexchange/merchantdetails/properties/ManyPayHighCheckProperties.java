package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("many-pay-high-check")
public record ManyPayHighCheckProperties(String url, String token) implements ManyPayProperties {
}
