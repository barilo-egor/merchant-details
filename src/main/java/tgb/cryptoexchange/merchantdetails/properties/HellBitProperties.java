package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hell-bit")
public record HellBitProperties(String url, String token, String signKey) implements PayBoxProperties {

}
