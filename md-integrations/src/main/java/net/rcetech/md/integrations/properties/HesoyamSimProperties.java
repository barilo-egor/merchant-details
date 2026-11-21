package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hesoyam-sim")
public record HesoyamSimProperties(String url, String key, String terminal) implements GambitProperties {
}
