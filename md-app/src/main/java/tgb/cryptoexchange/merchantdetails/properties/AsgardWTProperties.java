package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asgard-wt")
public record AsgardWTProperties(String url, String token, String secret,
                                 String merchantId) implements AsgardProperties {
}
