package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ya-pay")
public record YaPayProperties(String url, String token, String signKey) {
}
