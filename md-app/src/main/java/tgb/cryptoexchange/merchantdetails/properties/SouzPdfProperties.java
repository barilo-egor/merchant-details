package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "souz-pdf")
public record SouzPdfProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
