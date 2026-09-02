package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tron-ex-pdf")
public record TronExPdfProperties(String url, String key) implements TronExProperties {
}
