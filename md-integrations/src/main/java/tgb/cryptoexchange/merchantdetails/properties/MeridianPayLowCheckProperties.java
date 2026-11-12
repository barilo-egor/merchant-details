package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian-pay-low-check")
public record MeridianPayLowCheckProperties(String url, String token, String merchantId) implements LevelPayProperties {
}
