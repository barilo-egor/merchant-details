package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow-high-check")
public record PayscrowHighCheckProperties(String url, String key) implements PayscrowProperties {
}
