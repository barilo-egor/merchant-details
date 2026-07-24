package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("neural-pay")
public record NeuralPayProperties(String url, String token) {
}
