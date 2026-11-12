package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pw-pay")
public record PwPayProperties(String url, String token, String signKey) implements PayBoxProperties {
}
