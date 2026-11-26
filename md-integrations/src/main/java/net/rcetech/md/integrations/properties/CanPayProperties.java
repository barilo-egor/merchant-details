package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "can-pay")
public record CanPayProperties(String url, String apiKey) {
}
