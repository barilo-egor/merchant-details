package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nice-pay")
public record NicePayProperties(String url, String merchantId, String secret) {
}
