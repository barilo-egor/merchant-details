package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yolo-sim")
public record YoloSimProperties(String accountId, String url, String storeKey,
                                Credentials credentials) implements YoloProperties {
}
