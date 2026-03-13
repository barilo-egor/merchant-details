package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extasy-pay-qr")
public record ExtasyPayQRProperties(String url, String token, String signKey) implements PayBoxProperties {
}
