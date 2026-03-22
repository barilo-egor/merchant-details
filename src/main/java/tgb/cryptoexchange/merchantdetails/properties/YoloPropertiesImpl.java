package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yolo")
public record YoloPropertiesImpl(String accountId, String url, String storeKey,
                                 Credentials credentials) implements YoloProperties {
}
