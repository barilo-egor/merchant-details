package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bucks-pay-sim")
public record BucksPaySimProperties(String url, String token, String signKey) implements PayBoxProperties {
}
