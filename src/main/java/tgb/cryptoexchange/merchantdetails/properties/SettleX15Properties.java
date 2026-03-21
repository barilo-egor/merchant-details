package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("settle-x15")
public record SettleX15Properties(String url, String key, String sbpId, String c2cId) implements SettleXProperties {
}
