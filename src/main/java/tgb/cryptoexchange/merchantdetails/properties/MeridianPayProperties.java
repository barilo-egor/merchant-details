package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian-pay")
public record MeridianPayProperties(String url, String token, String merchantId) implements LevelPayProperties {
}
