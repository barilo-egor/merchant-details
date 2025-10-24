package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "panda-pay")
public record PandaPayProperties(String url, String key, String secret) {
}
