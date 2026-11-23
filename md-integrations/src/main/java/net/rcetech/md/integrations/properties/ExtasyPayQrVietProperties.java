package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extasy-pay-qr-viet")
public record ExtasyPayQrVietProperties(String url, String token, String key,
                                        String secret) implements BridgePayProperties {
}
