package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "honey-money")
public record HoneyMoneyProperties(Urls urls, String secret, String clientId, String signKey) {
    public record Urls(String main, String token) {}
}
