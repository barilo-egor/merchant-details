package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bucks-pay-sim")
public record BucksPaySimProperties(String url, String key, String secret,
                                    String shopId) implements BucksPayProperties {
}
