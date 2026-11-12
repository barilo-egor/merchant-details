package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asgard-high-check")
public record AsgardHighCheckProperties(String url, String token, String secret,
                                        String merchantId) implements AsgardProperties {
}
