package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extasy-pay-receipt")
public record ExtasyPayReceiptProperties(String url, String token, String signKey) implements PayBoxProperties {
}
