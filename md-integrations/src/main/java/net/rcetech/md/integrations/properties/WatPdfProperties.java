package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wat-pdf")
public record WatPdfProperties(String url, String token) implements WatProperties {
}
