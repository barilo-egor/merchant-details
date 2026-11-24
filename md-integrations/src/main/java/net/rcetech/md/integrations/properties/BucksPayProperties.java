package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bucks-pay")
public record BucksPayProperties(String url, String token, String signKey) implements PayBoxProperties {
}
