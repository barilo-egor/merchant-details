package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pay-crown")
public record PayCrownProperties(String url, String key, String secret, String merchantId) {
}
