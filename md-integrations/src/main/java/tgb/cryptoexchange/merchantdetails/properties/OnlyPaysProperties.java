package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "only-pays")
public record OnlyPaysProperties(String url, String secret, String id) {
}
