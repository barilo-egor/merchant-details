package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lotrien")
public record LotrienPropertiesImpl(String url, String key) implements LotrienProperties {
}
