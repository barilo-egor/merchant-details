package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wat-sim")
public record WatSimProperties(String url, String token) implements WatProperties {
}
