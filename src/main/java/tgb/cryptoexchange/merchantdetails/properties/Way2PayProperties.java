package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "way2pay")
public record Way2PayProperties(String url, String pub, String secret, String environment) {
}
