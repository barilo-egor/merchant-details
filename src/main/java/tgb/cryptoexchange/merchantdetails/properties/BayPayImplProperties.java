package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bay-pay")
public record BayPayImplProperties(String url, String apiKey) implements BayPayProperties {
}
