package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bit-bolide-deep-link")
public record BitBolideDeepLinkProperties(String url, String token, String signKey) implements PayBoxProperties {

}
