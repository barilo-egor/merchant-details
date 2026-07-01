package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bucks-pay")
public record BucksPayPropertiesImpl(String url, String key, String qrKey, String tPayKey, String secret, String qrSecret,
                                     String tPaySecret, String shopId, String qrShopId, String tPayShopId)
        implements BucksPayProperties {
}
