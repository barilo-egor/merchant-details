package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fiat-cut")
public record FiatCutProperties(String url, String token, String merchantId) {}
