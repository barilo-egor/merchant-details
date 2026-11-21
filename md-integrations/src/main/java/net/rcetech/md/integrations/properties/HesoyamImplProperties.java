package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hesoyam")
public record HesoyamImplProperties(String url, String key, String terminal) implements GambitProperties {
}
