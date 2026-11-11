package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("many-pay-low-check")
public record ManyPayLowCheckProperties(String url, String token) implements ManyPayProperties {
}
