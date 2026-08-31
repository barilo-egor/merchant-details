package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rs-pay-bt")
public record RSPayBTProperties(String url, String apiKey, String secret) implements RSPayProperties {
}
