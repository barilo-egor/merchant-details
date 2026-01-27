package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("studio")
public record StudioProperties(String url, String keyCard, String keySbp) {
}
