package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aurora-pay")
public record AuroraPayProperties(String url, String token, String merchantId) implements LevelPayProperties {
}
