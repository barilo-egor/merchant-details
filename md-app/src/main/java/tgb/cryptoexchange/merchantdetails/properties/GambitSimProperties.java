package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gambit-sim")
public record GambitSimProperties(String url, String key, String terminal) implements GambitProperties {
}
