package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bay-pay-low-check")
public record BayPayLowCheckProperties(String url, String apiKey) implements BayPayProperties {
}
