package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "souz-sim")
public record SouzSimProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
