package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deora-pdf")
public record DeoraPdfProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
