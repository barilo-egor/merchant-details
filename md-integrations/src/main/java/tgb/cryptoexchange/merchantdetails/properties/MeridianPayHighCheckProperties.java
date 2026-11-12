package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian-pay-high-check")
public record MeridianPayHighCheckProperties(String url, String token,
                                             String merchantId) implements LevelPayProperties {
}
