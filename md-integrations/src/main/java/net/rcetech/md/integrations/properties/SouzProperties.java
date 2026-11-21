package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "souz")
public record SouzProperties(String url, String token, String key, String secret) implements BridgePayProperties {
}
