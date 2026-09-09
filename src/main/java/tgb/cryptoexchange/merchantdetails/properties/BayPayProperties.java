package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bay-pay")
public record BayPayProperties(String url, String apiKey) {
}
