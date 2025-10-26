package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pulsar")
public record PulsarProperties(String url, String code, String secret, String token) {
}
