package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bit-bolide-wt")
public record BitBolideWTProperties(String url, String token, String signKey) implements PayBoxProperties {

}
