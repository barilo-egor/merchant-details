package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rs-pay")
public record RSPayImplProperties(String url, String apiKey, String secret) implements RSPayProperties {
}
