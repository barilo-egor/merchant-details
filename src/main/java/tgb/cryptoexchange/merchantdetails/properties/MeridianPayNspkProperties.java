package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian-pay-nspk")
public record MeridianPayNspkProperties(String url, String token, String merchantId) implements LevelPayProperties {
}
