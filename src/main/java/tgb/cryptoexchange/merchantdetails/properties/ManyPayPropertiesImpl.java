package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("many-pay")
public record ManyPayPropertiesImpl(String url, String token) implements ManyPayProperties {
}
