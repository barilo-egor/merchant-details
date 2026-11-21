package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bay-pay-high-check")
public record BayPayHighCheckProperties(String url, String apiKey) implements BayPayProperties {
}
