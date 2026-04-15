package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gambit")
public record GambitImplProperties(String url, String key, String terminal) implements GambitProperties {
}
