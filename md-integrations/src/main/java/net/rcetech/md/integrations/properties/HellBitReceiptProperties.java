package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hell-bit-receipt")
public record HellBitReceiptProperties(String url, String token, String signKey) implements PayBoxProperties {

}
