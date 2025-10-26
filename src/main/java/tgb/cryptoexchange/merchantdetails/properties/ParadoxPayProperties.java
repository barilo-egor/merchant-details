package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paradox-pay")
public record ParadoxPayProperties(String url, String token, String merchantId) {
}
