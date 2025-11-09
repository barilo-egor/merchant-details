package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "honey-money")
public record HoneyMoneyProperties(String url, String clientId, String authToken, String signToken) {
}
