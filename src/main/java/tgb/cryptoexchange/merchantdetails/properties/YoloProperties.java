package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yolo")
public record YoloProperties(String accountId, String url, String secretKey, Credentials credentials) {
    public record Credentials(
            String login,
            String verificationCode,
            String passphrase
    ) {
    }
}
