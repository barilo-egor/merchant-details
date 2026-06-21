package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bucks-pay-high-check")
public record BucksPayHighCheckProperties(String url, String key, String secret,
                                          String shopId) implements BucksPayProperties {
}
