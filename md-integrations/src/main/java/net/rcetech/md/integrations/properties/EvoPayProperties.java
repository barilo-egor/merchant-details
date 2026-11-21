package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evo-pay")
public record EvoPayProperties (String url, String key, String changeKey) {
}
