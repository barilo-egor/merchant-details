package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payscrow-transgran")
public record PayscrowTransgranProperties(String url, String key) implements PayscrowProperties {
}
