package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plata18")
public record Plata18Properties(String url, String token, String merchantId) implements LevelPayProperties{
}
