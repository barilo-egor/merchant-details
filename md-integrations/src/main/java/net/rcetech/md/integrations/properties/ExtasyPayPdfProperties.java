package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extasy-pay-pdf")
public record ExtasyPayPdfProperties(String url, String token, String key,
                                     String secret) implements BridgePayProperties {
}
