package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian-pay-sim")
public record MeridianPaySimProperties(String url, String token, String merchantId) implements LevelPayProperties {
}
