package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pay-lee-qr")
public record PayLeeQRProperties(String url, String token, String secret, String clientIdSalt) implements PayLeeProperties {
}
