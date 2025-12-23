package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pay-lee")
public record PayLeePropertiesImpl(String url, String token, String secret, String clientIdSalt) implements PayLeeProperties {
}
