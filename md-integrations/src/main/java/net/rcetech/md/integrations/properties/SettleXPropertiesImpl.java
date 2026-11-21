package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("settle-x")
public record SettleXPropertiesImpl(String url, String key, String sbpId, String c2cId) implements SettleXProperties {
}
