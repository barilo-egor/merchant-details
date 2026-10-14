package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smack-pay")
public record SmackPayProperties(String url, String apiKey) {
}
